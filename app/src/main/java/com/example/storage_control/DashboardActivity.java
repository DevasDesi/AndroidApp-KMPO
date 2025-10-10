package com.example.storage_control;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView tabOrders, tabProducts, tabAnalytics;
    private LinearLayout emptyOrdersLayout, ordersContainer;
    private View navHome, navOrders, navProducts, navAnalytics, navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        setupClickListeners();
        showEmptyState();
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
    }

    private void setupClickListeners() {
        // Табы
        tabOrders.setOnClickListener(v -> switchTab(tabOrders));
        tabProducts.setOnClickListener(v -> switchTab(tabProducts));
        tabAnalytics.setOnClickListener(v -> switchTab(tabAnalytics));

        // Навигация
        navHome.setOnClickListener(v -> switchNavigation(navHome));
        navOrders.setOnClickListener(v -> switchNavigation(navOrders));
        navProducts.setOnClickListener(v -> switchNavigation(navProducts));
        navAnalytics.setOnClickListener(v -> switchNavigation(navAnalytics));
        navSettings.setOnClickListener(v -> switchNavigation(navSettings));

        // Кнопка "Все"
        findViewById(R.id.viewAllButton).setOnClickListener(v -> {
            Toast.makeText(this, "Показать все заказы", Toast.LENGTH_SHORT).show();
            // TODO: Реализовать позже
        });
    }

    private void switchTab(TextView selectedTab) {
        // Сброс всех табов
        tabOrders.setBackgroundResource(R.drawable.tab_background);
        tabProducts.setBackgroundResource(R.drawable.tab_background);
        tabAnalytics.setBackgroundResource(R.drawable.tab_background);

        tabOrders.setTextColor(getColor(R.color.gray_dark));
        tabProducts.setTextColor(getColor(R.color.gray_dark));
        tabAnalytics.setTextColor(getColor(R.color.gray_dark));

        // Выбор активного таба
        selectedTab.setBackgroundResource(R.drawable.tab_background_selected);
        selectedTab.setTextColor(getColor(R.color.purple_500));

        // TODO: Здесь будет переключение контента между табами
        String tabName = selectedTab.getText().toString();
        Toast.makeText(this, "Выбрана вкладка: " + tabName, Toast.LENGTH_SHORT).show();
    }

    private void switchNavigation(View selectedNav) {
        // Сброс цветов навигации
        resetNavigationColors();

        // Установка активного элемента
        ImageView icon = (ImageView) ((LinearLayout) selectedNav).getChildAt(0);
        TextView text = (TextView) ((LinearLayout) selectedNav).getChildAt(1);

        icon.setColorFilter(getColor(R.color.purple_500));
        text.setTextColor(getColor(R.color.purple_500));

        // TODO: Здесь будет навигация между экранами
        String navName = text.getText().toString();
        Toast.makeText(this, "Навигация: " + navName, Toast.LENGTH_SHORT).show();
    }

    private void resetNavigationColors() {
        int grayColor = getColor(R.color.gray_dark);

        // Сброс всех иконок и текстов
        ImageView homeIcon = (ImageView) ((LinearLayout) navHome).getChildAt(0);
        TextView homeText = (TextView) ((LinearLayout) navHome).getChildAt(1);
        homeIcon.setColorFilter(grayColor);
        homeText.setTextColor(grayColor);

        // Аналогично для остальных элементов навигации...
    }

    private void showEmptyState() {
        emptyOrdersLayout.setVisibility(View.VISIBLE);
        ordersContainer.setVisibility(View.GONE);
    }

    // TODO: Этот метод будет заполнять заказы когда появится БД
    private void loadOrders() {
        // Показываем пустое состояние
        showEmptyState();

        // В будущем здесь будет:
        // 1. Получение заказов из БД
        // 2. Если заказы есть - скрыть emptyOrdersLayout
        // 3. Показать ordersContainer
        // 4. Динамически создать карточки заказов
    }
}