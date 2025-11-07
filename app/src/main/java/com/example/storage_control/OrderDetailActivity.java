package com.example.storage_control;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView orderIdView, customerNameView, orderAmountView, orderStatusView, orderDateView;
    private TextView customerPhoneView, customerAddressView, orderItemsView, deliveryDateView;
    private Button backButton, editButton;

    private int orderId;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
        loadOrderDetails();
    }

    private void initViews() {
        // Основная информация
        orderIdView = findViewById(R.id.orderId);
        customerNameView = findViewById(R.id.customerName);
        orderAmountView = findViewById(R.id.orderAmount);
        orderStatusView = findViewById(R.id.orderStatus);
        orderDateView = findViewById(R.id.orderDate);

        // Дополнительная информация
        customerPhoneView = findViewById(R.id.customerPhone);
        customerAddressView = findViewById(R.id.customerAddress);
        orderItemsView = findViewById(R.id.orderItems);
        deliveryDateView = findViewById(R.id.deliveryDate);

        // Кнопки
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetailActivity.this, EditOrderActivity.class);
            intent.putExtra("order_id", orderId);
            startActivityForResult(intent, 1);
        });
    }

    private void loadOrderDetails() {
        // Получаем ID заказа из Intent
        orderId = getIntent().getIntExtra("order_id", -1);

        if (orderId != -1) {
            Cursor cursor = databaseHelper.getOrderById(orderId);
            if (cursor.moveToFirst()) {
                // Заполняем основную информацию
                orderIdView.setText(cursor.getString(cursor.getColumnIndexOrThrow("order_id")));
                customerNameView.setText(cursor.getString(cursor.getColumnIndexOrThrow("customer_name")));
                orderAmountView.setText(String.format("%,d ₽", (int) cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))));
                orderStatusView.setText(getStatusText(cursor.getString(cursor.getColumnIndexOrThrow("status"))));
                orderDateView.setText(cursor.getString(cursor.getColumnIndexOrThrow("date")));

                // Заполняем дополнительную информацию
                customerPhoneView.setText(cursor.getString(cursor.getColumnIndexOrThrow("customer_phone")));
                customerAddressView.setText(cursor.getString(cursor.getColumnIndexOrThrow("customer_address")));
                orderItemsView.setText(cursor.getString(cursor.getColumnIndexOrThrow("order_items")));
                deliveryDateView.setText(cursor.getString(cursor.getColumnIndexOrThrow("delivery_date")));

                // Устанавливаем цвет статуса
                setStatusColor(cursor.getString(cursor.getColumnIndexOrThrow("status")));
            }
            cursor.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Обновляем данные после редактирования
            loadOrderDetails();
        }
    }

    private String getStatusText(String status){
        switch(status){
            case "new": return "Новый";
            case "processint": return "В процессе";
            case "delivered": return "Доставлен";
            case "overdue": return "Просрочен";
            default: return "Неизвестно";
        }
    }

    private String formatDate(String date){
        return date != null ? date : "Не указана";
    }

    private void setStatusColor(String Status){
        int color;
        switch (Status){
            case "new": color = R.color.status_new; break;
            case "processint": color = R.color.status_processing;break;
            case "delivered": color = R.color.status_delivered;break;
            case "overdue": color = R.color.status_overdue;break;
            default:color = R.color.gray_dark;
        }
        orderStatusView.setTextColor(ContextCompat.getColor(this, color));
    }
}
