package com.example.storage_control;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView totalOrders, totalProducts, criticalCount;
    private DatabaseHelper db;

    private LinearLayout navHome, navOrders, navProducts, navAnalytics, navProfile;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        db = new DatabaseHelper(this);

        initViews();
        loadUserRole();

        // ROLE REDIRECTS
        if (!userRole.equals("analytics_manager")) {

            if (userRole.equals("orders_manager")) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return;
            }

            if (userRole.equals("products_manager")) {
                startActivity(new Intent(this, ProductsActivity.class));
                finish();
                return;
            }

            Toast.makeText(this, "Ошибка роли", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupBottomMenu();
        loadAnalytics();
        highlightActive(navAnalytics);
    }

    private void initViews() {
        totalOrders = findViewById(R.id.totalOrders);
        totalProducts = findViewById(R.id.totalProducts);
        criticalCount = findViewById(R.id.criticalCount);

        navHome = findViewById(R.id.navHome);
        navOrders = findViewById(R.id.navOrders);
        navProducts = findViewById(R.id.navProducts);
        navAnalytics = findViewById(R.id.navAnalytics);
        navProfile = findViewById(R.id.navProfile);
    }

    private void loadUserRole() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        userRole = prefs.getString("role", "unknown");
    }

    private void setupBottomMenu() {

        navHome.setOnClickListener(v -> {
            Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
            resetNav();
            highlightActive(navAnalytics);
        });

        navOrders.setOnClickListener(v -> {
            Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
            resetNav();
            highlightActive(navAnalytics);
        });

        navProducts.setOnClickListener(v -> {
            Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
            resetNav();
            highlightActive(navAnalytics);
        });

        navAnalytics.setOnClickListener(v -> {
            resetNav();
            highlightActive(navAnalytics);
            loadAnalytics();
        });

        navProfile.setOnClickListener(v -> {
            resetNav();
            highlightActive(navProfile);

            Intent i = new Intent(this, ProfileActivity.class);
            startActivity(i);
        });
    }

    private void loadAnalytics() {
        int ordersCount = db.getOrdersCount();
        int productsCount = db.getProductsCount();
        int critical = db.getCriticalProductsCount();

        totalOrders.setText(String.valueOf(ordersCount));
        totalProducts.setText(String.valueOf(productsCount));
        criticalCount.setText(String.valueOf(critical));
    }

    private void resetNav() {
        int gray = ContextCompat.getColor(this, R.color.gray_dark);

        setNavColor(navHome, gray);
        setNavColor(navOrders, gray);
        setNavColor(navProducts, gray);
        setNavColor(navAnalytics, gray);
        setNavColor(navProfile, gray);

        navHome.setAlpha(0.5f);
        navOrders.setAlpha(0.5f);
        navProducts.setAlpha(0.5f);
        navAnalytics.setAlpha(0.5f);
        navProfile.setAlpha(0.5f);
    }

    private void highlightActive(LinearLayout v) {
        v.setAlpha(1f);
        int activeColor = ContextCompat.getColor(this, R.color.black);
        setNavColor(v, activeColor);
    }

    private void setNavColor(LinearLayout nav, int color) {
        if (nav.getChildCount() >= 2) {
            android.widget.ImageView icon = (android.widget.ImageView) nav.getChildAt(0);
            TextView text = (TextView) nav.getChildAt(1);
            icon.setColorFilter(color);
            text.setTextColor(color);
        }
    }
}
