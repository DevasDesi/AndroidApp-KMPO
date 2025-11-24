package com.example.storage_control;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView nameView, skuView, categoryView, priceView, stockView, minStockView, descriptionView;
    private Button deleteButton;

    private DatabaseHelper databaseHelper;
    private String currentSku = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        databaseHelper = new DatabaseHelper(this);

        initViews();

        currentSku = getIntent().getStringExtra("sku");

        if (currentSku == null || currentSku.isEmpty()) {
            Toast.makeText(this, "Ошибка: SKU не передан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProduct(currentSku);
        setupDeleteButton();
    }

    private void initViews() {
        nameView = findViewById(R.id.detailName);
        skuView = findViewById(R.id.detailSku);
        categoryView = findViewById(R.id.detailCategory);
        priceView = findViewById(R.id.detailPrice);
        stockView = findViewById(R.id.detailStock);
        minStockView = findViewById(R.id.detailMinStock);
        descriptionView = findViewById(R.id.detailDescription);

        deleteButton = findViewById(R.id.deleteButton);
    }

    private void loadProduct(String sku) {
        Cursor cursor = databaseHelper.getProductBySku(sku);

        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Товар не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String name = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
        String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
        int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"));
        int minStock = cursor.getInt(cursor.getColumnIndexOrThrow("min_stock"));
        String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

        nameView.setText(name);
        skuView.setText(sku);
        categoryView.setText(category);
        priceView.setText(String.format("%,.0f ₽", price));
        stockView.setText(stock + " шт.");
        minStockView.setText(minStock + " шт.");
        descriptionView.setText(description.isEmpty() ? "Нет описания" : description);

        cursor.close();
    }

    private void setupDeleteButton() {
        deleteButton.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Удалить товар?")
                    .setMessage("Вы действительно хотите удалить этот товар?")
                    .setPositiveButton("Удалить", (dialog, which) -> {

                        boolean deleted = databaseHelper.deleteProduct(currentSku);

                        if (deleted) {
                            Toast.makeText(this, "Товар удалён", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                        }

                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }
}
