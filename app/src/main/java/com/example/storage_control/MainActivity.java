package com.example.storage_control;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private Spinner roleSpinner;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupRoleSpinner();
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
        roleSpinner = findViewById(R.id.roleSpinner);

        // Поля входа
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
    }

    private void setupRoleSpinner() {
        // Роли для выбора
        String[] roles = {
                "Менеджер заказов",
                "Менеджер товаров",
                "Аналитик"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
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
        String selectedRole = roleSpinner.getSelectedItem().toString();

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

        // Конвертация роли в системный формат
        String systemRole = convertRoleToSystem(selectedRole);

        // Регистрация
        if (databaseHelper.registerUserWithRole(name, email, password, systemRole)) {
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();

            // Сохраняем данные пользователя
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            prefs.edit()
                    .putString("name", name)
                    .putString("email", email)
                    .putString("role", systemRole)
                    .apply();

            // Переход на соответствующий экран
            redirectByRole(systemRole);
        } else {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
        }
    }

    private String convertRoleToSystem(String selectedRole) {
        switch (selectedRole) {
            case "Менеджер заказов":
                return "orders_manager";
            case "Менеджер товаров":
                return "products_manager";
            case "Аналитик":
                return "analytics_manager";
            default:
                return "user";
        }
    }

    private void redirectByRole(String role) {
        Intent intent;

        switch (role) {
            case "orders_manager":
                intent = new Intent(MainActivity.this, DashboardActivity.class);
                break;
            case "products_manager":
                intent = new Intent(MainActivity.this, ProductsActivity.class);
                break;
            case "analytics_manager":
                intent = new Intent(MainActivity.this, AnalyticsActivity.class);
                break;
            default:
                Toast.makeText(this, "Неизвестная роль", Toast.LENGTH_SHORT).show();
                return;
        }

        startActivity(intent);
        finish();
    }

    private void loginUser() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkUser(email, password)) {

            android.database.Cursor cursor = databaseHelper.getUserByEmail(email);

            String name = "Неизвестно";
            String role = "user";

            if (cursor != null && cursor.moveToFirst()) {
                int idxName = cursor.getColumnIndex("name");
                int idxRole = cursor.getColumnIndex("role");

                if (idxName != -1) name = cursor.getString(idxName);
                if (idxRole != -1) role = cursor.getString(idxRole);

                cursor.close();
            }

            // Сохраняем данные
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            prefs.edit()
                    .putString("name", name)
                    .putString("email", email)
                    .putString("role", role)
                    .apply();

            // Перенаправление по роли
            redirectByRole(role);

        } else {
            Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
        }
    }
}