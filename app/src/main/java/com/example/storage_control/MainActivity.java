package com.example.storage_control;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText regName, regEmail, regPassword, regConfirmPassword;
    private EditText loginEmail, loginPassword;
    private Button registerButton, loginButton;
    private CheckBox termsCheckbox;
    private LinearLayout loginForm, registerForm;
    private TextView loginTab, registerTab;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        // Формы
        loginForm = findViewById(R.id.loginForm);
        registerForm = findViewById(R.id.registerForm);

        // Табы
        loginTab = findViewById(R.id.loginTab);
        registerTab = findViewById(R.id.registerTab);

        // Поля регистрации
        regName = findViewById(R.id.regName);
        regEmail = findViewById(R.id.regEmail);
        regPassword = findViewById(R.id.regPassword);
        regConfirmPassword = findViewById(R.id.regConfirmPassword);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        registerButton = findViewById(R.id.registerButton);

        // Поля входа
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
    }

    private void setupClickListeners() {
        // Переключение табов
        loginTab.setOnClickListener(v -> showLoginForm());
        registerTab.setOnClickListener(v -> showRegisterForm());

        // Регистрация
        registerButton.setOnClickListener(v -> registerUser());

        // Вход
        loginButton.setOnClickListener(v -> loginUser());
    }

    private void showLoginForm() {
        loginForm.setVisibility(View.VISIBLE);
        registerForm.setVisibility(View.GONE);
        loginTab.setBackgroundResource(R.drawable.tab_background_selected);
        registerTab.setBackgroundResource(R.drawable.tab_background);
        loginTab.setTextColor(getColor(R.color.purple_500));
        registerTab.setTextColor(getColor(R.color.gray_dark));
    }

    private void showRegisterForm() {
        registerForm.setVisibility(View.VISIBLE);
        loginForm.setVisibility(View.GONE);
        registerTab.setBackgroundResource(R.drawable.tab_background_selected);
        loginTab.setBackgroundResource(R.drawable.tab_background);
        registerTab.setTextColor(getColor(R.color.purple_500));
        loginTab.setTextColor(getColor(R.color.gray_dark));
    }

    private void registerUser() {
        String name = regName.getText().toString().trim();
        String email = regEmail.getText().toString().trim();
        String password = regPassword.getText().toString().trim();
        String confirmPassword = regConfirmPassword.getText().toString().trim();

        // Валидация
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "Примите условия использования", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка email
        if (databaseHelper.checkEmailExists(email)) {
            Toast.makeText(this, "Email уже зарегистрирован", Toast.LENGTH_SHORT).show();
            return;
        }

        // Регистрация
        if (databaseHelper.registerUser(name, email, password)) {
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
            // Переход на главный экран
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginUser() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkUser(email, password)) {
            Toast.makeText(this, "Вход выполнен!", Toast.LENGTH_SHORT).show();
            // Переход на главный экран
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
        }
    }
}