package com.example.storage_control;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;

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

    private void openOrderDetails(String orderId, String customerName, double amount, String status) {
        // Получаем ID заказа из базы данных
        Cursor cursor = databaseHelper.getAllOrders();
        int actualOrderId = -1;

        if (cursor.moveToFirst()) {
            do {
                String currentOrderId = cursor.getString(cursor.getColumnIndexOrThrow("order_id"));
                if (currentOrderId.equals(orderId)) {
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
        // Реальные счетчики из базы данных
        int overdueCount = databaseHelper.getOrdersCountByStatus("overdue");
        int processingCount = databaseHelper.getOrdersCountByStatus("processing");
        int criticalStockCount = databaseHelper.getCriticalStockCount();

        overdueOrdersCount.setText(String.valueOf(overdueCount));
        pendingOrdersCount.setText(String.valueOf(processingCount));
        this.criticalStockCount.setText(String.valueOf(criticalStockCount));
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
                // Уже на главной, ничего не делаем
                break;
            case "orders":
                setNavActive(navOrders);
                switchTab("orders");
                break;
            case "products":
                setNavActive(navProducts);
                // ПЕРЕХОДИМ НА ProductsActivity
                startActivity(new Intent(DashboardActivity.this, ProductsActivity.class));
                finish(); // Закрываем текущую активити
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
    }

    private void showCriticalStock() {
        switchTab("products");

        // Показываем товары с критическим остатком
        Cursor cursor = databaseHelper.getCriticalStockProducts();
        if (cursor.getCount() > 0) {
            displayProducts(cursor);
            TextView sectionTitle = findViewById(R.id.sectionTitle);
            sectionTitle.setText("Товары с критическим остатком");
        } else {
            showEmptyProductsState();
        }
    }

    private void showPendingOrders() {
        switchTab("orders");
        Cursor cursor = databaseHelper.getOrdersByStatus("processing");
        displayOrders(cursor, "Заказы в обработке");
    }

    private void showAllOrders() {
        Cursor cursor = databaseHelper.getAllOrders();
        displayOrders(cursor, "Все заказы");
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
        orderIdView.setTypeface(null, Typeface.BOLD);

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
        customerView.setTypeface(null, Typeface.BOLD);

        TextView amountView = new TextView(this);
        amountView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        amountView.setText(String.format(Locale.getDefault(), "%,d ₽", (int) amount));
        amountView.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
        amountView.setTextSize(16);
        amountView.setTypeface(null, Typeface.BOLD);

        detailsRow.addView(customerView);
        detailsRow.addView(amountView);

        card.addView(headerRow);
        card.addView(detailsRow);
        card.setOnClickListener(v -> {
            openOrderDetails(orderId, customerName, amount, status);
        });

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

    // ==================== ФУНКЦИОНАЛ ТОВАРОВ ====================

    private void showProductsScreen() {
        emptyOrdersLayout.setVisibility(View.GONE);
        ordersContainer.setVisibility(View.VISIBLE);
        ordersContainer.removeAllViews();

        // Заголовок
        TextView sectionTitle = findViewById(R.id.sectionTitle);
        sectionTitle.setText("Все товары");

        // Кнопка добавления товара
        Button addProductButton = new Button(this);
        addProductButton.setText("+ Добавить товар");
        addProductButton.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background));
        addProductButton.setTextColor(Color.WHITE);
        addProductButton.setTextSize(16);
        addProductButton.setPadding(32, 16, 32, 16);
        addProductButton.setOnClickListener(v -> showAddProductDialog());

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 0, 0, 16);
        addProductButton.setLayoutParams(buttonParams);

        ordersContainer.addView(addProductButton);

        // Загружаем товары из БД
        loadProducts();
    }

    private void loadProducts() {
        Cursor cursor = databaseHelper.getAllProducts();
        if (cursor.getCount() > 0) {
            displayProducts(cursor);
        } else {
            showEmptyProductsState();
        }
    }

    private void displayProducts(Cursor cursor) {
        if (cursor.moveToFirst()) {
            do {
                String productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                String sku = cursor.getString(cursor.getColumnIndexOrThrow("sku"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"));
                int minStock = cursor.getInt(cursor.getColumnIndexOrThrow("min_stock"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));

                View productCard = createProductCard(productName, sku, price, stock, minStock, category);
                ordersContainer.addView(productCard);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private View createProductCard(String productName, String sku, double price,
                                   int stock, int minStock, String category) {
        // Создаем карточку товара
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.order_card_background));
        card.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        // Название товара и артикул
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView nameView = new TextView(this);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        nameView.setText(productName);
        nameView.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        nameView.setTextSize(16);
        nameView.setTypeface(null, Typeface.BOLD);

        TextView skuView = new TextView(this);
        skuView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        skuView.setText(sku);
        skuView.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        skuView.setTextSize(12);

        headerRow.addView(nameView);
        headerRow.addView(skuView);

        // Категория
        TextView categoryView = new TextView(this);
        categoryView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        categoryView.setText(category);
        categoryView.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
        categoryView.setTextSize(12);
        categoryView.setPadding(0, 8, 0, 0);

        // Цена и остаток
        LinearLayout detailsRow = new LinearLayout(this);
        detailsRow.setOrientation(LinearLayout.HORIZONTAL);
        detailsRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        detailsRow.setPadding(0, 12, 0, 0);

        TextView priceView = new TextView(this);
        priceView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        priceView.setText(String.format(Locale.getDefault(), "%,d ₽", (int) price));
        priceView.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        priceView.setTextSize(14);
        priceView.setTypeface(null, Typeface.BOLD);

        TextView stockView = new TextView(this);
        stockView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        stockView.setText("Остаток: " + stock + " шт.");
        stockView.setTextColor(getStockColor(stock, minStock));
        stockView.setTextSize(14);
        stockView.setTypeface(null, Typeface.BOLD);

        detailsRow.addView(priceView);
        detailsRow.addView(stockView);

        card.addView(headerRow);
        card.addView(categoryView);
        card.addView(detailsRow);

        return card;
    }

    private int getStockColor(int stock, int minStock) {
        if (stock == 0) {
            return ContextCompat.getColor(this, R.color.stock_critical); // Красный
        } else if (stock <= minStock) {
            return ContextCompat.getColor(this, R.color.stock_low); // Желтый
        } else {
            return ContextCompat.getColor(this, R.color.stock_normal); // Зеленый
        }
    }

    private void showEmptyProductsState() {
        TextView message = new TextView(this);
        message.setText("Товары не найдены\nНажмите 'Добавить товар' для начала");
        message.setTextSize(16);
        message.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        message.setGravity(Gravity.CENTER);
        message.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        ordersContainer.addView(message);
    }



    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить товар");

        // Создаем layout для диалога
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Название товара");
        nameInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        nameInput.setPadding(16, 16, 16, 16);
        layout.addView(nameInput);

        final EditText skuInput = new EditText(this);
        skuInput.setHint("Артикул (SKU)");
        skuInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        skuInput.setPadding(16, 16, 16, 16);
        layout.addView(skuInput);

        final EditText priceInput = new EditText(this);
        priceInput.setHint("Цена");
        priceInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        priceInput.setPadding(16, 16, 16, 16);
        layout.addView(priceInput);

        final EditText stockInput = new EditText(this);
        stockInput.setHint("Количество на складе");
        stockInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        stockInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        stockInput.setPadding(16, 16, 16, 16);
        layout.addView(stockInput);

        builder.setView(layout);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String sku = skuInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String stockStr = stockInput.getText().toString().trim();

            if (name.isEmpty() || sku.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                int minStock = Math.max(1, stock / 4); // Автоматически устанавливаем мин. запас

                boolean success = databaseHelper.addProduct(name, sku, price, stock, minStock, "Разное", "");
                if (success) {
                    Toast.makeText(this, "Товар добавлен", Toast.LENGTH_SHORT).show();
                    // Обновляем список товаров
                    showProductsScreen();
                } else {
                    Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректные данные", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showAnalyticsScreen() {
        emptyOrdersLayout.setVisibility(View.GONE);
        ordersContainer.setVisibility(View.VISIBLE);
        ordersContainer.removeAllViews();

        TextView message = new TextView(this);
        message.setText("Раздел 'Аналитика' в разработке");
        message.setTextSize(18);
        message.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        message.setGravity(Gravity.CENTER);
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