package com.example.weathertcpapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    TableLayout tableLayout;
    Button viewMapBtn;
    String userId = "admin";
    String password = "password123";

    StringBuilder mapDataBuilder = new StringBuilder(); // To collect data for map

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        tableLayout = findViewById(R.id.tableLayout);
        viewMapBtn = findViewById(R.id.viewMapBtn);  // You’ll add this in XML

        fetchTemperatureData();

        viewMapBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("temperatureData", mapDataBuilder.toString()); // Send data to MapActivity
            startActivity(intent);
        });
    }

    private void fetchTemperatureData() {
        new Thread(() -> {
            try {
                Socket socket = new Socket("10.0.2.2", 65432);  // Update to your IP if needed

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(userId + ":" + password);

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }

                socket.close();

                String[] rows = response.toString().split("\n");

                runOnUiThread(() -> {
                    if (rows.length > 0 && !rows[0].equalsIgnoreCase("INVALID")) {
                        for (String row : rows) {
                            if (row.contains(":")) {
                                String[] parts = row.split(":");
                                if (parts.length == 2) {
                                    String location = parts[0].trim();
                                    String temperature = parts[1].trim();
                                    addRow(location, temperature);
                                    mapDataBuilder.append(location).append(":").append(temperature).append(";");
                                }
                            }
                        }
                    } else {
                        addRow("Login", "Failed or Invalid Data");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> addRow("Error", e.getMessage()));
            }
        }).start();
    }

    private void addRow(String location, String temperature) {
        TableRow tableRow = new TableRow(this);

        TextView locView = new TextView(this);
        locView.setText(location);
        locView.setPadding(10, 10, 10, 10);
        locView.setTextSize(16);

        TextView tempView = new TextView(this);
        tempView.setText(temperature + "°C");
        tempView.setPadding(10, 10, 10, 10);
        tempView.setTextSize(16);

        tableRow.addView(locView);
        tableRow.addView(tempView);

        tableLayout.addView(tableRow);
    }
}
