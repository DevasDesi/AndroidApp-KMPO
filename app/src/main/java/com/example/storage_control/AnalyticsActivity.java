package com.example.storage_control;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView totalOrders, totalProducts, criticalCount, completedOrdersRevenue;
    private BarChart revenueChart;
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
        setupRevenueChart();
        highlightActive(navAnalytics);
    }

    private void initViews() {
        totalOrders = findViewById(R.id.totalOrders);
        totalProducts = findViewById(R.id.totalProducts);
        criticalCount = findViewById(R.id.criticalCount);
        completedOrdersRevenue = findViewById(R.id.completedOrdersRevenue);
        revenueChart = findViewById(R.id.revenueChart);

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
            setupRevenueChart();
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
        double revenue = db.getCompletedOrdersRevenue();

        totalOrders.setText(String.valueOf(ordersCount));
        totalProducts.setText(String.valueOf(productsCount));
        criticalCount.setText(String.valueOf(critical));
        completedOrdersRevenue.setText(String.format("%.2f ₽", revenue));
    }

    private void setupRevenueChart() {
        // Получаем данные о доходах по месяцам
        Map<String, Double> monthlyRevenue = db.getMonthlyRevenue();

        if (monthlyRevenue.isEmpty()) {
            revenueChart.setNoDataText("Нет данных о доходах");
            revenueChart.setNoDataTextColor(Color.GRAY);
            return;
        }

        // Подготавливаем данные для графика
        List<BarEntry> entries = new ArrayList<>();
        final List<String> months = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Double> entry : monthlyRevenue.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            months.add(entry.getKey());
            index++;
        }

        // Создаем набор данных
        BarDataSet dataSet = new BarDataSet(entries, "Доходы по месяцам");
        dataSet.setColor(Color.parseColor("#4f46e5"));
        dataSet.setValueTextColor(Color.parseColor("#1e293b"));
        dataSet.setValueTextSize(12f);

        // Форматируем значения в рублях
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f ₽", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        // Настраиваем ось X
        XAxis xAxis = revenueChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(months.size());
        xAxis.setTextColor(Color.parseColor("#475569"));
        xAxis.setDrawGridLines(false);

        // Настраиваем ось Y
        YAxis leftAxis = revenueChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#475569"));
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f ₽", value);
            }
        });

        YAxis rightAxis = revenueChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Другие настройки графика
        revenueChart.setData(barData);
        revenueChart.getDescription().setEnabled(false);
        revenueChart.getLegend().setEnabled(false);
        revenueChart.setDrawGridBackground(false);
        revenueChart.setDrawBorders(false);
        revenueChart.setTouchEnabled(true);
        revenueChart.setDragEnabled(true);
        revenueChart.setScaleEnabled(true);
        revenueChart.setPinchZoom(true);

        // Анимация
        revenueChart.animateY(1000);

        // Обновляем график
        revenueChart.invalidate();
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