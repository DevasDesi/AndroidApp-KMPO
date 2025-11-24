package com.example.storage_control;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userEmail, userRole;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userRole = findViewById(R.id.userRole);
        logoutButton = findViewById(R.id.logoutButton);

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        String name = prefs.getString("name", "Не указано");
        String email = prefs.getString("email", "Не указано");
        String role = prefs.getString("role", "пользователь");

        userName.setText(name);
        userEmail.setText(email);
        userRole.setText("Роль: " + role);

        logoutButton.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
