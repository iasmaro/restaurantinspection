package com.carbon.restaurantinspection.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.carbon.restaurantinspection.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.carbon.restaurantinspection.model.InspectionDetail;
import com.carbon.restaurantinspection.model.InspectionManager;
import com.carbon.restaurantinspection.model.Restaurant;
import com.carbon.restaurantinspection.model.RestaurantManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Hashtable;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    private int restaurant_index = 0;
    private Hashtable <String, Integer> markers;
    private Hashtable <String, Integer> restaurant_index_holder;
    private Marker currentMarker;
    private Marker myMarker;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Map");

        markers = new Hashtable<>();
        restaurant_index_holder = new Hashtable<>();

        getLocationPermission();
    }

    // gets the Restaurant and Inspection Lists and helps set markers where appropriate

    private void setRestaurantMarkers() {

       List<Restaurant> restaurantList = RestaurantManager.getInstance(this).getRestaurantList();

        if(restaurantList != null){
            int num_of_restaurants = restaurantList.size();
            for(int i = 0; i < num_of_restaurants; i++) {
               Restaurant restaurant = restaurantList.get(i);
                String trackingNum = restaurant.getTrackingNumber();
                List<InspectionDetail> inspectionDetailList = InspectionManager.getInstance(this)
                        .getInspections(trackingNum);
                float latitude = (float) restaurant.getLatitude();
                float longitude = (float) restaurant.getLongitude();
                String name = restaurant.getName();

                if(inspectionDetailList != null) {
                    int size = inspectionDetailList.size();
                    InspectionDetail inspectionDetail = inspectionDetailList.get(size - 1);
                    moveCamera(new LatLng(latitude, longitude), DEFAULT_ZOOM, inspectionDetail,
                            restaurant);
                }
                else{
                    String address = restaurant.getPhysicalAddress();
                    moveCamera(new LatLng(latitude, longitude), DEFAULT_ZOOM, name, address);
                }
            }
        }
}

    // moves the camera to the location of the chosen restaurant given the restaurant has no
    // inspections
    private void moveCamera(LatLng latLng, float zoom, String title, String address){
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("Current location")) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .snippet(address);
            myMarker =  googleMap.addMarker(markerOptions);
           markers.put(myMarker.getId(), 0);
           restaurant_index_holder.put(myMarker.getId(), restaurant_index);
           restaurant_index++;
        }
    }

    // moves the camera to the location of the chosen restaurant given the restaurant has an
    // inspection
    private void moveCamera(LatLng latLng, float zoom, InspectionDetail inspectionDetail,
                            Restaurant restaurant) {

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(inspectionDetail != null){
            String snippet = "Address: " + restaurant.getPhysicalAddress() + "\n\n" +
                    "Hazard level " + inspectionDetail.getHazardLevel();

            String hazardLevel = inspectionDetail.getHazardLevel();
            int image_id;

            if (hazardLevel.equals("High")) {
                image_id = R.drawable.red_skull_crossbones;
            }
            else if (hazardLevel.equals("Moderate")) {
                image_id = R.drawable.ic_warning_yellow_24dp;
            }
            else {
                image_id = R.drawable.greencheckmark;
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(restaurant.getName())
                    .snippet(snippet);
             myMarker = googleMap.addMarker(markerOptions);
            markers.put(myMarker.getId(), image_id);
            restaurant_index_holder.put(myMarker.getId(), restaurant_index);
            restaurant_index++;
            googleMap.setInfoWindowAdapter(new ExtraInfoWindowAdapter(MapActivity.this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                startActivity(new Intent(this, RestaurantListActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: sucess");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initializeMap();

            } else {
                ActivityCompat.requestPermissions(this, permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if(grantResults.length > 0){
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    //initializeMap();
                    finish();
                    startActivity(getIntent());
                }
            }
        }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.d(TAG, "onMapReady: success");
        if (mLocationPermissionsGranted == true) {
            // when permission is granted, get the current location
            getCurrentLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onMapReady: NOT WORKING");
                return;
            }
            googleMap.setMyLocationEnabled(true);
            this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            setRestaurantMarkers();

            // checks if marker has been clicked and goes to RestaurantDetailsActivity if it has
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if(currentMarker != null) {
                        if (myMarker == currentMarker) {
                            int index = restaurant_index_holder.get(marker.getId());
                            Intent intent = RestaurantDetailsActivity.makeIntent(MapActivity.this,
                                    index);
                            startActivity(intent);
                            currentMarker = null;
                        }
                        else{
                            currentMarker = myMarker;
                        }
                    }
                    else{
                        currentMarker = myMarker;
                    }
                    return false;
                }
            });
        }
    }

    private void getCurrentLocation() {

        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            LatLng myLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera(myLatLng, DEFAULT_ZOOM);

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: success");
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    /**
     *  Class that creates the pop up display when a marker is clicked
     */
    class ExtraInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View view;
        private Context context;

        public ExtraInfoWindowAdapter(Context context) {
            this.context = context;
            view = LayoutInflater.from(context).inflate(R.layout.extra_info_window, null);
        }

        private void rendowWindowText(final Marker marker, View view1) {
            String title = marker.getTitle();
            TextView textView = view1.findViewById(R.id.extra_title);

            if (!title.equals("")) {
                textView.setText(title);
            }

            String snippet = marker.getSnippet();
            TextView textViewSnippet = view1.findViewById(R.id.snippet);

            if (snippet != null && !snippet.equals("")) {
                textViewSnippet.setText(snippet);
            }

            ImageView imageView = view1.findViewById(R.id.hazard_level);

            if (marker.getId() != null && markers != null && markers.size() > 0) {
                int image_id = markers.get(marker.getId());
                if (image_id != 0) {
                    imageView.setImageResource(image_id);
                } else {
                    imageView.setImageResource(R.drawable.ic_warning_yellow_24dp);
                }
            }
        }

        @Override
        public View getInfoWindow(Marker marker) {
            rendowWindowText(marker, view);
            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {
            rendowWindowText(marker, view);
            return null;
        }
    }
}
