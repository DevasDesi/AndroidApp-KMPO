package com.example.storage_control;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;

public class EditOrderActivity extends AppCompatActivity {

    private EditText customerNameEdit, customerPhoneEdit, customerAddressEdit;
    private EditText orderAmountEdit, orderItemsEdit, deliveryDateEdit;
    private Spinner statusSpinner;
    private Button saveButton, cancelButton;

    private int orderId;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_order);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupSpinner();
        loadOrderData();
        setupClickListeners();
    }

    private void initViews() {
        customerNameEdit = findViewById(R.id.customerNameEdit);
        customerPhoneEdit = findViewById(R.id.customerPhoneEdit);
        customerAddressEdit = findViewById(R.id.customerAddressEdit);
        orderAmountEdit = findViewById(R.id.orderAmountEdit);
        orderItemsEdit = findViewById(R.id.orderItemsEdit);
        deliveryDateEdit = findViewById(R.id.deliveryDateEdit);
        statusSpinner = findViewById(R.id.statusSpinner);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.order_statuses, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);
    }

    private void loadOrderData() {
        // Получаем ID заказа из Intent
        orderId = getIntent().getIntExtra("order_id", -1);

        if (orderId != -1) {
            // Загружаем данные заказа из БД
            Cursor cursor = databaseHelper.getOrderById(orderId);
            if (cursor.moveToFirst()) {
                customerNameEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow("customer_name")));
                customerPhoneEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow("customer_phone")));
                customerAddressEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow("customer_address")));
                orderAmountEdit.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))));
                orderItemsEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow("order_items")));
                deliveryDateEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow("delivery_date")));

                // Устанавливаем статус в спиннер
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                setSpinnerSelection(status);
            }
            cursor.close();
        }
    }

    private void setSpinnerSelection(String status) {
        String[] statuses = getResources().getStringArray(R.array.order_statuses);
        for (int i = 0; i < statuses.length; i++) {
            if (getStatusKey(statuses[i]).equals(status)) {
                statusSpinner.setSelection(i);
                break;
            }
        }
    }

    private String getStatusKey(String statusText) {
        switch (statusText) {
            case "Новый": return "new";
            case "В обработке": return "processing";
            case "Доставлен": return "delivered";
            case "Просрочен": return "overdue";
            default: return "new";
        }
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveOrder());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void saveOrder() {
        // Валидация полей
        if (customerNameEdit.getText().toString().trim().isEmpty() ||
                customerPhoneEdit.getText().toString().trim().isEmpty() ||
                orderAmountEdit.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String customerName = customerNameEdit.getText().toString().trim();
            String phone = customerPhoneEdit.getText().toString().trim();
            String address = customerAddressEdit.getText().toString().trim();
            double amount = Double.parseDouble(orderAmountEdit.getText().toString().trim());
            String items = orderItemsEdit.getText().toString().trim();
            String deliveryDate = deliveryDateEdit.getText().toString().trim();
            String status = getStatusKey(statusSpinner.getSelectedItem().toString());

            // Сохраняем в БД
            boolean success = databaseHelper.updateOrder(orderId, customerName, phone, address,
                    amount, status, items, deliveryDate);

            if (success) {
                Toast.makeText(this, "Заказ обновлен", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректная сумма", Toast.LENGTH_SHORT).show();
        }
    }
}