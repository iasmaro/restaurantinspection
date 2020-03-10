
package com.carbon.restaurantinspection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);
        Intent intent = InspectionDetails.makeIntent(this, 0, "SDFO-8HKP7E");
        startActivity(intent);
    }
}