package com.example.storage_control;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import java.util.Locale;

public class ProductsActivity extends AppCompatActivity {

    private TextView criticalStockCount, lowStockCount, totalProductsCount;
    private TextView tabAllProducts, tabCriticalStock, tabLowStock, tabByCategory;
    private TextView productsSectionTitle;
    private LinearLayout emptyProductsLayout, productsContainer;
    private Button addProductButton;
    private View navHome, navOrders, navProducts, navAnalytics, navSettings;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
        updateBadgeCounts();
        loadAllProducts();
    }

    private void initViews() {
        // Бейджи счетчиков
        criticalStockCount = findViewById(R.id.criticalStockCount);
        lowStockCount = findViewById(R.id.lowStockCount);
        totalProductsCount = findViewById(R.id.totalProductsCount);

        // Табы
        tabAllProducts = findViewById(R.id.tabAllProducts);
        tabCriticalStock = findViewById(R.id.tabCriticalStock);
        tabLowStock = findViewById(R.id.tabLowStock);
        tabByCategory = findViewById(R.id.tabByCategory);

        // Контейнеры
        emptyProductsLayout = findViewById(R.id.emptyProductsLayout);
        productsContainer = findViewById(R.id.productsContainer);
        productsSectionTitle = findViewById(R.id.productsSectionTitle);

        // Кнопки
        addProductButton = findViewById(R.id.addProductButton);

        // Навигация
        navHome = findViewById(R.id.navHome);
        navOrders = findViewById(R.id.navOrders);
        navProducts = findViewById(R.id.navProducts);
        navAnalytics = findViewById(R.id.navAnalytics);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupClickListeners() {
        // Табы
        tabAllProducts.setOnClickListener(v -> switchTab("all"));
        tabCriticalStock.setOnClickListener(v -> switchTab("critical"));
        tabLowStock.setOnClickListener(v -> switchTab("low"));
        tabByCategory.setOnClickListener(v -> switchTab("category"));

        // Кнопка добавления
        addProductButton.setOnClickListener(v -> showAddProductDialog());

        // Навигация
        navHome.setOnClickListener(v -> switchNavigation("home"));
        navOrders.setOnClickListener(v -> switchNavigation("orders"));
        navProducts.setOnClickListener(v -> switchNavigation("products"));
        navAnalytics.setOnClickListener(v -> switchNavigation("analytics"));
        navSettings.setOnClickListener(v -> switchNavigation("settings"));
    }

    private void switchTab(String tab) {
        // Сброс всех табов
        resetTabs();

        switch (tab) {
            case "all":
                tabAllProducts.setBackgroundResource(R.drawable.tab_background_selected);
                tabAllProducts.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
                productsSectionTitle.setText("Все товары");
                loadAllProducts();
                break;
            case "critical":
                tabCriticalStock.setBackgroundResource(R.drawable.tab_background_selected);
                tabCriticalStock.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
                productsSectionTitle.setText("Товары с критическим остатком");
                loadCriticalStockProducts();
                break;
            case "low":
                tabLowStock.setBackgroundResource(R.drawable.tab_background_selected);
                tabLowStock.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
                productsSectionTitle.setText("Товары с низким остатком");
                loadLowStockProducts();
                break;
            case "category":
                tabByCategory.setBackgroundResource(R.drawable.tab_background_selected);
                tabByCategory.setTextColor(ContextCompat.getColor(this, R.color.purple_500));
                productsSectionTitle.setText("Товары по категориям");
                loadProductsByCategory();
                break;
        }
    }

    private void resetTabs() {
        tabAllProducts.setBackgroundResource(R.drawable.tab_background);
        tabCriticalStock.setBackgroundResource(R.drawable.tab_background);
        tabLowStock.setBackgroundResource(R.drawable.tab_background);
        tabByCategory.setBackgroundResource(R.drawable.tab_background);

        tabAllProducts.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        tabCriticalStock.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        tabLowStock.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        tabByCategory.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
    }

    private void switchNavigation(String navItem) {
        switch (navItem) {
            case "home":
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                break;
            case "orders":
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                break;
            case "products":
                // Уже на товарах
                break;
            case "analytics":
                Toast.makeText(this, "Аналитика - в разработке", Toast.LENGTH_SHORT).show();
                break;
            case "settings":
                Toast.makeText(this, "Настройки - в разработке", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void updateBadgeCounts() {
        // Здесь будут реальные данные из БД
        criticalStockCount.setText("2");
        lowStockCount.setText("3");
        totalProductsCount.setText("15");
    }

    private void loadAllProducts() {
        Cursor cursor = databaseHelper.getAllProducts();
        displayProducts(cursor);
    }

    private void loadCriticalStockProducts() {
        Cursor cursor = databaseHelper.getCriticalStockProducts();
        displayProducts(cursor);
    }

    private void loadLowStockProducts() {
        // Нужно добавить метод в DatabaseHelper
        Cursor cursor = databaseHelper.getAllProducts(); // временно
        displayProducts(cursor);
    }

    private void loadProductsByCategory() {
        Cursor cursor = databaseHelper.getAllProducts(); // временно
        displayProducts(cursor);
    }

    private void displayProducts(Cursor cursor) {
        emptyProductsLayout.setVisibility(View.GONE);
        productsContainer.setVisibility(View.VISIBLE);
        productsContainer.removeAllViews();

        if (cursor.moveToFirst()) {
            do {
                String productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                String sku = cursor.getString(cursor.getColumnIndexOrThrow("sku"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"));
                int minStock = cursor.getInt(cursor.getColumnIndexOrThrow("min_stock"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));

                View productCard = createProductCard(productName, sku, price, stock, minStock, category);
                productsContainer.addView(productCard);

            } while (cursor.moveToNext());
        } else {
            showEmptyState();
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
        nameView.setMaxLines(2);
        nameView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView skuView = new TextView(this);
        skuView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        skuView.setText(sku);
        skuView.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        skuView.setTextSize(12);
        skuView.setBackground(ContextCompat.getDrawable(this, R.drawable.status_background));
        skuView.setPadding(8, 4, 8, 4);

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
        categoryView.setTypeface(null, Typeface.BOLD);
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
        stockView.setText(stock + " шт.");
        stockView.setTextColor(getStockColor(stock, minStock));
        stockView.setTextSize(14);
        stockView.setTypeface(null, Typeface.BOLD);
        stockView.setBackground(getStockBackground(stock, minStock));
        stockView.setPadding(12, 6, 12, 6);

        detailsRow.addView(priceView);
        detailsRow.addView(stockView);

        // Статус запаса
        TextView stockStatusView = new TextView(this);
        stockStatusView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        stockStatusView.setText(getStockStatusText(stock, minStock));
        stockStatusView.setTextColor(getStockColor(stock, minStock));
        stockStatusView.setTextSize(11);
        stockStatusView.setPadding(0, 8, 0, 0);

        card.addView(headerRow);
        card.addView(categoryView);
        card.addView(detailsRow);
        card.addView(stockStatusView);

        // Добавляем обработчик клика на карточку
        card.setOnClickListener(v -> {
            // Здесь можно добавить переход к редактированию товара
            Toast.makeText(this, "Товар: " + productName, Toast.LENGTH_SHORT).show();
        });

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

    private android.graphics.drawable.Drawable getStockBackground(int stock, int minStock) {
        int color;
        if (stock == 0) {
            color = R.color.status_overdue;
        } else if (stock <= minStock) {
            color = R.color.status_processing;
        } else {
            color = R.color.status_delivered;
        }
        return ContextCompat.getDrawable(this, color);
    }

    private String getStockStatusText(int stock, int minStock) {
        if (stock == 0) {
            return "Нет в наличии";
        } else if (stock <= minStock) {
            return "Заканчивается";
        } else {
            return "В наличии";
        }
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить товар");

        // Создаем layout для диалога
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        // Название товара
        final EditText nameInput = new EditText(this);
        nameInput.setHint("Название товара *");
        nameInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        nameInput.setPadding(16, 16, 16, 16);
        nameInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(nameInput);

        // Артикул
        final EditText skuInput = new EditText(this);
        skuInput.setHint("Артикул (SKU) *");
        skuInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        skuInput.setPadding(16, 16, 16, 16);
        skuInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(skuInput);

        // Категория
        final EditText categoryInput = new EditText(this);
        categoryInput.setHint("Категория");
        categoryInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        categoryInput.setPadding(16, 16, 16, 16);
        categoryInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(categoryInput);

        // Цена
        final EditText priceInput = new EditText(this);
        priceInput.setHint("Цена *");
        priceInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        priceInput.setPadding(16, 16, 16, 16);
        priceInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(priceInput);

        // Количество на складе
        final EditText stockInput = new EditText(this);
        stockInput.setHint("Количество на складе *");
        stockInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        stockInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        stockInput.setPadding(16, 16, 16, 16);
        stockInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(stockInput);

        // Минимальный запас
        final EditText minStockInput = new EditText(this);
        minStockInput.setHint("Минимальный запас");
        minStockInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        minStockInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        minStockInput.setPadding(16, 16, 16, 16);
        minStockInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(minStockInput);

        // Описание
        final EditText descriptionInput = new EditText(this);
        descriptionInput.setHint("Описание");
        descriptionInput.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));
        descriptionInput.setPadding(16, 16, 16, 16);
        descriptionInput.setMinLines(2);
        descriptionInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(descriptionInput);

        // Добавляем отступы между полями
        for (int i = 0; i < layout.getChildCount(); i++) {
            if (i > 0) {
                View child = layout.getChildAt(i);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                params.topMargin = 12;
                child.setLayoutParams(params);
            }
        }

        builder.setView(layout);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String sku = skuInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String stockStr = stockInput.getText().toString().trim();
            String minStockStr = minStockInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();

            // Валидация обязательных полей
            if (name.isEmpty() || sku.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "Заполните обязательные поля (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                int minStock = minStockStr.isEmpty() ? Math.max(1, stock / 4) : Integer.parseInt(minStockStr);

                if (category.isEmpty()) {
                    category = "Разное";
                }

                boolean success = databaseHelper.addProduct(name, sku, price, stock, minStock, category, description);
                if (success) {
                    Toast.makeText(this, "Товар добавлен", Toast.LENGTH_SHORT).show();
                    // Обновляем список товаров
                    loadAllProducts();
                    updateBadgeCounts();
                } else {
                    Toast.makeText(this, "Ошибка при добавлении. Возможно артикул уже существует", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректные числовые данные", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEmptyState() {
        emptyProductsLayout.setVisibility(View.VISIBLE);
        productsContainer.setVisibility(View.GONE);
    }
}