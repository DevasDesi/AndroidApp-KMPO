package com.example.storage_control;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView sectionTitle;
    private LinearLayout emptyOrdersLayout, ordersContainer;

    private View navHome, navOrders, navProducts, navAnalytics;
    private LinearLayout navProfile;

    private LinearLayout overdueBadge, criticalBadge, pendingBadge;
    private TextView overdueOrdersCount, criticalStockCount, pendingOrdersCount;

    private DatabaseHelper databaseHelper;

    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);
        initViews();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        userRole = prefs.getString("role", "unknown");

        // ================================
        // ROLE REDIRECT — ПОЛНЫЙ ПОРЯДОК
        // ================================
        if (!userRole.equals("orders_manager")) {

            if (userRole.equals("products_manager")) {
                startActivity(new Intent(this, ProductsActivity.class));
                finish();
                return;
            }

            if (userRole.equals("analytics_manager")) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                finish();
                return;
            }

            Toast.makeText(this, "Ошибка роли", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Только заказной менеджер остаётся здесь.
        setupClickListeners();
        updateBadgeCounts();
        loadRecentOrders();
        setNavActive(navHome);
    }

    private void initViews() {
        sectionTitle = findViewById(R.id.sectionTitle);
        emptyOrdersLayout = findViewById(R.id.emptyOrdersLayout);
        ordersContainer = findViewById(R.id.ordersContainer);

        navHome = findViewById(R.id.navHome);
        navOrders = findViewById(R.id.navOrders);
        navProducts = findViewById(R.id.navProducts);
        navAnalytics = findViewById(R.id.navAnalytics);
        navProfile = findViewById(R.id.navProfile);

        overdueBadge = findViewById(R.id.overdueBadge);
        criticalBadge = findViewById(R.id.criticalBadge);
        pendingBadge = findViewById(R.id.pendingBadge);

        overdueOrdersCount = findViewById(R.id.overdueOrdersCount);
        criticalStockCount = findViewById(R.id.criticalStockCount);
        pendingOrdersCount = findViewById(R.id.pendingOrdersCount);
    }

    private void setupClickListeners() {

        overdueBadge.setOnClickListener(v -> showOverdueOrders());
        criticalBadge.setOnClickListener(v -> showCriticalStock());
        pendingBadge.setOnClickListener(v -> showPendingOrders());

        findViewById(R.id.viewAllButton).setOnClickListener(v -> showAllOrders());

        navHome.setOnClickListener(v -> {
            resetNavigationColors();
            setNavActive(navHome);
            switchToDashboard();
        });

        navOrders.setOnClickListener(v -> {
            resetNavigationColors();
            setNavActive(navOrders);
            switchToDashboard();
        });

        navProducts.setOnClickListener(v -> {
            if (!userRole.equals("orders_manager")) {
                Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
                resetNavigationColors();
                setNavActive(navHome);
                return;
            }
            resetNavigationColors();
            setNavActive(navProducts);
            startActivity(new Intent(this, ProductsActivity.class));
        });

        navAnalytics.setOnClickListener(v -> {
            if (!userRole.equals("orders_manager")) {
                Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
                resetNavigationColors();
                setNavActive(navHome);
                return;
            }
            resetNavigationColors();
            setNavActive(navAnalytics);
            startActivity(new Intent(this, AnalyticsActivity.class));
        });

        navProfile.setOnClickListener(v -> {
            resetNavigationColors();
            setNavActive(navProfile);
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    private void switchToDashboard() {
        updateBadgeCounts();
        loadRecentOrders();
    }

    private void openOrderDetails(String orderId, String customerName, double amount, String status) {
        Cursor cursor = databaseHelper.getAllOrders();
        int actualOrderId = -1;

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndexOrThrow("order_id")).equals(orderId)) {
                    actualOrderId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (actualOrderId != -1) {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order_id", actualOrderId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Ошибка: заказ не найден", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBadgeCounts() {
        int overdueCount = databaseHelper.getOrdersCountByStatus("overdue");
        int processingCount = databaseHelper.getOrdersCountByStatus("processing");
        int criticalStock = databaseHelper.getCriticalStockCount();

        overdueOrdersCount.setText(String.valueOf(overdueCount));
        pendingOrdersCount.setText(String.valueOf(processingCount));
        criticalStockCount.setText(String.valueOf(criticalStock));
    }

    private void loadRecentOrders() {
        sectionTitle.setText("Последние заказы");
        Cursor cursor = databaseHelper.getRecentOrders(3);

        if (cursor != null && cursor.getCount() > 0) {
            displayOrders(cursor, "Последние заказы");
        } else {
            showEmptyState();
        }
    }

    private void displayOrders(Cursor cursor, String title) {
        emptyOrdersLayout.setVisibility(View.GONE);
        ordersContainer.setVisibility(View.VISIBLE);
        ordersContainer.removeAllViews();

        sectionTitle.setText(title);

        if (cursor.moveToFirst()) {
            do {
                String orderId = cursor.getString(cursor.getColumnIndexOrThrow("order_id"));
                String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                View card = createOrderCard(orderId, customerName, amount, status);
                ordersContainer.addView(card);

            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    private View createOrderCard(String orderId, String customerName, double amount, String status) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.order_card_background));
        card.setPadding(32, 32, 32, 32);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView orderIdView = new TextView(this);
        orderIdView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        orderIdView.setText(orderId);
        orderIdView.setTextSize(16);
        orderIdView.setTypeface(null, Typeface.BOLD);

        TextView statusView = new TextView(this);
        statusView.setText(getStatusText(status));
        statusView.setPadding(16, 8, 16, 8);
        statusView.setTextSize(12);
        statusView.setBackground(getStatusBackground(status));

        headerRow.addView(orderIdView);
        headerRow.addView(statusView);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.HORIZONTAL);
        details.setPadding(0, 16, 0, 0);

        TextView customerView = new TextView(this);
        customerView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        customerView.setText(customerName);
        customerView.setTextSize(14);

        TextView amountView = new TextView(this);
        amountView.setText(String.format(Locale.getDefault(), "%,d ₽", (int) amount));
        amountView.setTextSize(16);
        amountView.setTypeface(null, Typeface.BOLD);

        details.addView(customerView);
        details.addView(amountView);

        card.addView(headerRow);
        card.addView(details);

        card.setOnClickListener(v -> openOrderDetails(orderId, customerName, amount, status));

        return card;
    }

    private String getStatusText(String status) {
        switch (status) {
            case "new": return "Новый";
            case "processing": return "В обработке";
            case "delivered": return "Доставлен";
            case "overdue": return "Просрочен";
            default: return "Неизвестно";
        }
    }

    private android.graphics.drawable.Drawable getStatusBackground(String status) {
        int color;
        switch (status) {
            case "new": color = R.color.status_new; break;
            case "processing": color = R.color.status_processing; break;
            case "delivered": color = R.color.status_delivered; break;
            case "overdue": color = R.color.status_overdue; break;
            default: color = R.color.gray_light;
        }
        return ContextCompat.getDrawable(this, color);
    }

    private void showOverdueOrders() {
        Cursor cursor = databaseHelper.getOrdersByStatus("overdue");
        if (cursor.getCount() > 0) displayOrders(cursor, "Просроченные заказы");
        else Toast.makeText(this, "Просроченных заказов нет", Toast.LENGTH_SHORT).show();
    }

    private void showCriticalStock() {
        Cursor cursor = databaseHelper.getCriticalStockProducts();
        if (cursor.getCount() > 0) {
            ordersContainer.removeAllViews();
            displayProducts(cursor);
            sectionTitle.setText("Товары с критическим остатком");
        } else {
            Toast.makeText(this, "Критических остатков нет", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPendingOrders() {
        Cursor cursor = databaseHelper.getOrdersByStatus("processing");
        if (cursor.getCount() > 0) displayOrders(cursor, "В обработке");
        else Toast.makeText(this, "Нет заказов в обработке", Toast.LENGTH_SHORT).show();
    }

    private void showAllOrders() {
        Cursor cursor = databaseHelper.getAllOrders();
        if (cursor.getCount() > 0) displayOrders(cursor, "Все заказы");
        else showEmptyState();
    }

    private void showEmptyState() {
        emptyOrdersLayout.setVisibility(View.VISIBLE);
        ordersContainer.setVisibility(View.GONE);
    }

    private void displayProducts(Cursor cursor) {
        ordersContainer.setVisibility(View.VISIBLE);
        ordersContainer.removeAllViews();

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                String sku = cursor.getString(cursor.getColumnIndexOrThrow("sku"));

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(ContextCompat.getDrawable(this, R.drawable.order_card_background));
                card.setPadding(32, 32, 32, 32);

                TextView n = new TextView(this);
                n.setText(name);
                n.setTextSize(16);
                n.setTypeface(null, Typeface.BOLD);

                TextView s = new TextView(this);
                s.setText(sku);
                s.setTextSize(12);

                card.addView(n);
                card.addView(s);

                ordersContainer.addView(card);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void resetNavigationColors() {
        int gray = ContextCompat.getColor(this, R.color.gray_dark);

        setNavColor(navHome, gray);
        setNavColor(navOrders, gray);
        setNavColor(navProducts, gray);
        setNavColor(navAnalytics, gray);
        setNavColor(navProfile, gray);
    }

    private void setNavColor(View nav, int color) {
        ImageView icon = (ImageView) ((LinearLayout) nav).getChildAt(0);
        TextView text = (TextView) ((LinearLayout) nav).getChildAt(1);
        icon.setColorFilter(color);
        text.setTextColor(color);
    }

    private void setNavActive(View navView) {
        navHome.setAlpha(0.5f);
        navOrders.setAlpha(0.5f);
        navProducts.setAlpha(0.5f);
        navAnalytics.setAlpha(0.5f);
        navProfile.setAlpha(0.5f);

        navView.setAlpha(1f);
    }
}
