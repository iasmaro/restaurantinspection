package com.carbon.restaurantinspection.model;

import android.content.Context;

import com.carbon.restaurantinspection.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;

/**
  InspectionManager class is a singleton class containing inspection details of restaurants.
 */
public class InspectionManager {
    private Hashtable<String, ArrayList<InspectionDetail>> inspections = new Hashtable<>();
    private static InspectionManager instance;

    public static InspectionManager getInstance(Context context) {
        if (instance == null) {
            instance = new InspectionManager(context);
        }
        return instance;
    }

    private InspectionManager(Context context) {
        File csvFile = context.getFileStreamPath("inspections.csv");
        CSVLoader loader = new CSVLoader();
        ArrayList<String> file;
        if (csvFile.isFile()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(csvFile);
                file = loader.readCSV(fileInputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                InputStream is = context.getResources().openRawResource(R.raw.inspectionreports_itr1);
                file = loader.readCSV(is);
            }
        } else {
            InputStream is = context.getResources().openRawResource(R.raw.inspectionreports_itr1);
            file = loader.readCSV(is);
        }
        parseFile(file);
        sortInspectionDetails();
    }

    private void sortInspectionDetails() {
        Set<String> trackingNumbers = inspections.keySet();
        for (String trackingNum : trackingNumbers) {
            ArrayList<InspectionDetail> inspectionList = inspections.get(trackingNum);
            Collections.sort(inspectionList, new Comparator<InspectionDetail>() {
                @Override
                public int compare(InspectionDetail inspectionDetail, InspectionDetail otherinspection) {
                    return otherinspection.getStrInspectionDate().compareTo(inspectionDetail.getStrInspectionDate());
                }
            });
        }
    }

    private void parseFile(ArrayList<String> file) {
        for (String line : file) {
            String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            addInspectionDetail(tokens);
        }
    }

    private void addInspectionDetail(String[] tokens) {
        String[] hazardList;
        int criticalIssues = Integer.parseInt(tokens[3]);
        int nonCriticalIssues = Integer.parseInt(tokens[4]);
        String[] violationsArray;
        if (tokens.length == 7) {
            String[] violationsList = tokens[5].split("\"");
            String violations;
            if (violationsList.length > 1) {
                violations = violationsList[1];
            } else {
                violations = violationsList[0];
            }
            violationsArray = violations.split("\\|");
             hazardList = tokens[6].split("\"");
        } else {
            violationsArray = null;
            if (tokens.length == 6) {
                hazardList = tokens[5].split("\"");
            } else {
                hazardList = null;
            }
        }
        String[] trackingNumberList = tokens[0].split("\"");
        String trackingNumber;
        if (trackingNumberList.length > 1) {
            trackingNumber = trackingNumberList[1];
        } else {
            trackingNumber = trackingNumberList[0];
        }
        String[] typeList = tokens[2].split("\"");
        String type;
        if (typeList.length > 1) {
            type = typeList[1];
        } else {
            type = typeList[0];
        }
        String hazard;
        if (hazardList == null) {
            hazard = "Low";
        } else if (hazardList.length > 1){
            hazard = hazardList[1];
        } else {
            hazard = hazardList[0];
        }
        InspectionDetail inspection = new InspectionDetail(trackingNumber, tokens[1], type,
                criticalIssues, nonCriticalIssues, hazard, violationsArray);
        if (inspections.containsKey(trackingNumber)) {
            inspections.get(trackingNumber).add(inspection);
        }
        else {
            ArrayList<InspectionDetail> inspectionList = new ArrayList<>();
            inspectionList.add(inspection);
            inspections.put(trackingNumber, inspectionList);
        }
    }

    public ArrayList<InspectionDetail> getInspections(String trackingNumber) {
        return inspections.get(trackingNumber);
    }
}
