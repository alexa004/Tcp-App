package com.example.weathertcpapp;

import android.graphics.Typeface;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    TableLayout tableLayout;
    Button btnViewMap;
    String tempData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tableLayout = findViewById(R.id.tableLayout);
        btnViewMap = findViewById(R.id.btnViewMap);  // ✅ Find the View Map button

        // ✅ Get temperature data from MainActivity (login screen)
        tempData = getIntent().getStringExtra("temperature_data");
        if (tempData != null && !tempData.isEmpty()) {
            populateTable(tempData);
        }

        // ✅ Set listener for "View Map" button
        btnViewMap.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MapActivity.class);
            intent.putExtra("temperature_data", tempData);
            startActivity(intent);
        });
    }

    private void populateTable(String data) {
        // Header row
        TableRow headerRow = new TableRow(this);

        TextView headerLocation = new TextView(this);
        headerLocation.setText("Location");
        headerLocation.setTextSize(16);
        headerLocation.setTypeface(null, Typeface.BOLD);
        headerLocation.setPadding(20, 20, 20, 20);

        TextView headerTemp = new TextView(this);
        headerTemp.setText("Temperature");
        headerTemp.setTextSize(16);
        headerTemp.setTypeface(null, Typeface.BOLD);
        headerTemp.setPadding(20, 20, 20, 20);

        headerRow.addView(headerLocation);
        headerRow.addView(headerTemp);

        tableLayout.addView(headerRow);

        // Data rows
        String[] lines = data.split("\n");

        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String location = parts[0].trim();
                String temp = parts[1].trim();

                TableRow row = new TableRow(this);

                TextView tvLoc = new TextView(this);
                tvLoc.setText(location);
                tvLoc.setPadding(20, 20, 20, 20);
                tvLoc.setTextSize(15);

                TextView tvTemp = new TextView(this);
                tvTemp.setText(temp + " °C");
                tvTemp.setPadding(20, 20, 20, 20);
                tvTemp.setTextSize(15);

                row.addView(tvLoc);
                row.addView(tvTemp);

                tableLayout.addView(row);
            }
        }
    }

}
