package com.example.weathertcpapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    ProgressBar progressBar;

    final String SERVER_IP = "10.0.2.2";  // <-- CHANGE this to your Python server IP
    final int SERVER_PORT = 65432;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Allow network on main thread (for simplicity)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter username & password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);

                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();

                // Send credentials
                String credentials = username + ":" + password;
                out.write(credentials.getBytes());
                out.flush();

                // Read response
                byte[] buffer = new byte[4096];
                int bytesRead = in.read(buffer);
                socket.close();

                String response = new String(buffer, 0, bytesRead).trim();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (response.equals("INVALID")) {
                        Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    } else {
                        // ✅ Valid login → go to DashboardActivity
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.putExtra("temperature_data", response);
                        startActivity(intent);
                        finish();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Connection Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
