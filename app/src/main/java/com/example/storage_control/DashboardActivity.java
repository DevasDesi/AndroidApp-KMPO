package com.example.storage_control;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class DashboardActivity extends AppCompatActivity {

    private TextView tabOrders, tabProducts, tabAnalytics;
    private LinearLayout emptyOrdersLayout, ordersContainer;
    private View navHome, navOrders, navProducts, navAnalytics, navSettings;
    private LinearLayout overdueBadge, criticalBadge, pendingBadge;
    private TextView overdueOrdersCount, criticalStockCount, pendingOrdersCount;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
        updateBadgeCounts();
        loadRecentOrders();
    }

    private void initViews() {
        // Табы
        tabOrders = findViewById(R.id.tabOrders);
        tabProducts = findViewById(R.id.tabProducts);
        tabAnalytics = findViewById(R.id.tabAnalytics);

        // Контейнеры заказов
        emptyOrdersLayout = findViewById(R.id.emptyOrdersLayout);
        ordersContainer = findViewById(R.id.ordersContainer);

        // Навигация
        navHome = findViewById(R.id.navHome);
        navOrders = findViewById(R.id.navOrders);
        navProducts = findViewById(R.id.navProducts);
        navAnalytics = findViewById(R.id.navAnalytics);
        navSettings = findViewById(R.id.navSettings);

        // Бейджи (верхние кнопки)
        overdueBadge = findViewById(R.id.overdueBadge);
        criticalBadge = findViewById(R.id.criticalBadge);
        pendingBadge = findViewById(R.id.pendingBadge);

        // Счетчики бейджей
        overdueOrdersCount = findViewById(R.id.overdueOrdersCount);
        criticalStockCount = findViewById(R.id.criticalStockCount);
        pendingOrdersCount = findViewById(R.id.pendingOrdersCount);
    }

    private void setupClickListeners() {
        // Табы
        tabOrders.setOnClickListener(v -> switchTab("orders"));
        tabProducts.setOnClickListener(v -> switchTab("products"));
        tabAnalytics.setOnClickListener(v -> switchTab("analytics"));

        // Навигация
        navHome.setOnClickListener(v -> switchNavigation("home"));
        navOrders.setOnClickListener(v -> switchNavigation("orders"));
        navProducts.setOnClickListener(v -> switchNavigation("products"));
        navAnalytics.setOnClickListener(v -> switchNavigation("analytics"));
        navSettings.setOnClickListener(v -> switchNavigation("settings"));

        // Верхние бейджи
        overdueBadge.setOnClickListener(v -> showOverdueOrders());
        criticalBadge.setOnClickListener(v -> showCriticalStock());
        pendingBadge.setOnClickListener(v -> showPendingOrders());

        // Кнопка "Все"
        findViewById(R.id.viewAllButton).setOnClickListener(v -> showAllOrders());
    }

    private void updateBadgeCounts() {
        // Реальные счетчики из базы данных
        int overdueCount = databaseHelper.getOrdersCountByStatus("overdue");
        int processingCount = databaseHelper.getOrdersCountByStatus("processing");

        overdueOrdersCount.setText(String.valueOf(overdueCount));
        pendingOrdersCount.setText(String.valueOf(processingCount));

        // Для критического остатка пока заглушка
        criticalStockCount.setText("1");
    }

    private void switchTab(String tab) {
        // Сброс всех табов
        resetTabs();

        switch (tab) {
            case "orders":
                tabOrders.setBackgroundResource(R.drawable.tab_background_selected);
                tabOrders.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
                loadRecentOrders();
                break;
            case "products":
                tabProducts.setBackgroundResource(R.drawable.tab_background_selected);
                tabProducts.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
                showProductsScreen();
                break;
            case "analytics":
                tabAnalytics.setBackgroundResource(R.drawable.tab_background_selected);
                tabAnalytics.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
                showAnalyticsScreen();
                break;
        }
    }

    private void resetTabs() {
        tabOrders.setBackgroundResource(R.drawable.tab_background);
        tabProducts.setBackgroundResource(R.drawable.tab_background);
        tabAnalytics.setBackgroundResource(R.drawable.tab_background);

        tabOrders.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        tabProducts.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        tabAnalytics.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
    }

    private void switchNavigation(String navItem) {
        // Сброс цветов навигации
        resetNavigationColors();

        // Установка активного элемента
        switch (navItem) {
            case "home":
                setNavActive(navHome);
                Toast.makeText(this, "Уже на главной", Toast.LENGTH_SHORT).show();
                break;
            case "orders":
                setNavActive(navOrders);
                switchTab("orders");
                break;
            case "products":
                setNavActive(navProducts);
                switchTab("products");
                break;
            case "analytics":
                setNavActive(navAnalytics);
                switchTab("analytics");
                break;
            case "settings":
                setNavActive(navSettings);
                Toast.makeText(this, "Настройки - в разработке", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setNavActive(View navView) {
        ImageView icon = (ImageView) ((LinearLayout) navView).getChildAt(0);
        TextView text = (TextView) ((LinearLayout) navView).getChildAt(1);

        icon.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));
        text.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
    }

    private void resetNavigationColors() {
        int grayColor = ContextCompat.getColor(this, R.color.gray_dark);

        ImageView homeIcon = (ImageView) ((LinearLayout) navHome).getChildAt(0);
        TextView homeText = (TextView) ((LinearLayout) navHome).getChildAt(1);
        homeIcon.setColorFilter(grayColor);
        homeText.setTextColor(grayColor);

        ImageView ordersIcon = (ImageView) ((LinearLayout) navOrders).getChildAt(0);
        TextView ordersText = (TextView) ((LinearLayout) navOrders).getChildAt(1);
        ordersIcon.setColorFilter(grayColor);
        ordersText.setTextColor(grayColor);

        ImageView productsIcon = (ImageView) ((LinearLayout) navProducts).getChildAt(0);
        TextView productsText = (TextView) ((LinearLayout) navProducts).getChildAt(1);
        productsIcon.setColorFilter(grayColor);
        productsText.setTextColor(grayColor);

        ImageView analyticsIcon = (ImageView) ((LinearLayout) navAnalytics).getChildAt(0);
        TextView analyticsText = (TextView) ((LinearLayout) navAnalytics).getChildAt(1);
        analyticsIcon.setColorFilter(grayColor);
        analyticsText.setTextColor(grayColor);

        ImageView settingsIcon = (ImageView) ((LinearLayout) navSettings).getChildAt(0);
        TextView settingsText = (TextView) ((LinearLayout) navSettings).getChildAt(1);
        settingsIcon.setColorFilter(grayColor);
        settingsText.setTextColor(grayColor);
    }

    // Функционал верхних бейджей
    private void showOverdueOrders() {
        switchTab("orders");
        Cursor cursor = databaseHelper.getOrdersByStatus("overdue");
        displayOrders(cursor, "Просроченные заказы");
        Toast.makeText(this, "Показаны просроченные заказы", Toast.LENGTH_SHORT).show();
    }

    private void showCriticalStock() {
        switchTab("products");
        Toast.makeText(this, "Показаны товары с критическим остатком", Toast.LENGTH_SHORT).show();
        // Здесь будет логика для товаров
    }

    private void showPendingOrders() {
        switchTab("orders");
        Cursor cursor = databaseHelper.getOrdersByStatus("processing");
        displayOrders(cursor, "Заказы в обработке");
        Toast.makeText(this, "Показаны заказы в обработке", Toast.LENGTH_SHORT).show();
    }

    private void showAllOrders() {
        Cursor cursor = databaseHelper.getAllOrders();
        displayOrders(cursor, "Все заказы");
        Toast.makeText(this, "Показаны все заказы", Toast.LENGTH_SHORT).show();
    }

    private void loadRecentOrders() {
        Cursor cursor = databaseHelper.getRecentOrders(3);
        if (cursor.getCount() > 0) {
            displayOrders(cursor, "Последние заказы");
        } else {
            showEmptyState();
        }
    }

    private void displayOrders(Cursor cursor, String title) {
        emptyOrdersLayout.setVisibility(View.GONE);
        ordersContainer.setVisibility(View.VISIBLE);
        ordersContainer.removeAllViews();

        // Обновляем заголовок
        TextView sectionTitle = findViewById(R.id.sectionTitle);
        sectionTitle.setText(title);

        if (cursor.moveToFirst()) {
            do {
                String orderId = cursor.getString(cursor.getColumnIndexOrThrow("order_id"));
                String customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

                // Создаем карточку заказа
                View orderCard = createOrderCard(orderId, customerName, amount, status);
                ordersContainer.addView(orderCard);

            } while (cursor.moveToNext());
        } else {
            showEmptyState();
        }
        cursor.close();
    }



    private View createOrderCard(String orderId, String customerName, double amount, String status) {
        // Создаем карточку программно
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

        // Верхняя строка - ID заказа и статус
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
        orderIdView.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        orderIdView.setTextSize(16);
        orderIdView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView statusView = new TextView(this);
        statusView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        statusView.setText(getStatusText(status));
        statusView.setPadding(16, 8, 16, 8);
        statusView.setTextSize(12);
        statusView.setBackground(getStatusBackground(status));

        headerRow.addView(orderIdView);
        headerRow.addView(statusView);

        // Нижняя строка - имя клиента и сумма
        LinearLayout detailsRow = new LinearLayout(this);
        detailsRow.setOrientation(LinearLayout.HORIZONTAL);
        detailsRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        detailsRow.setPadding(0, 16, 0, 0);

        TextView customerView = new TextView(this);
        customerView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        customerView.setText(customerName);
        customerView.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        customerView.setTextSize(14);
        customerView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView amountView = new TextView(this);
        amountView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        amountView.setText(String.format("%d ₽", (int) amount));
        amountView.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
        amountView.setTextSize(16);
        amountView.setTypeface(null, android.graphics.Typeface.BOLD);

        detailsRow.addView(customerView);
        detailsRow.addView(amountView);

        card.addView(headerRow);
        card.addView(detailsRow);

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

    private void showProductsScreen() {
        emptyOrdersLayout.setVisibility(View.GONE);
        ordersContainer.setVisibility(View.VISIBLE);
        ordersContainer.removeAllViews();

        TextView message = new TextView(this);
        message.setText("Раздел 'Товары' в разработке");
        message.setTextSize(18);
        message.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        message.setGravity(android.view.Gravity.CENTER);
        message.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        ordersContainer.addView(message);
    }

    private void showAnalyticsScreen() {
        emptyOrdersLayout.setVisibility(View.GONE);
        ordersContainer.setVisibility(View.VISIBLE);
        ordersContainer.removeAllViews();

        TextView message = new TextView(this);
        message.setText("Раздел 'Аналитика' в разработке");
        message.setTextSize(18);
        message.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        message.setGravity(android.view.Gravity.CENTER);
        message.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        ordersContainer.addView(message);
    }

    private void showEmptyState() {
        emptyOrdersLayout.setVisibility(View.VISIBLE);
        ordersContainer.setVisibility(View.GONE);
    }
}