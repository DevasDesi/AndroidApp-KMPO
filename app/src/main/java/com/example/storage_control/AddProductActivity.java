package com.example.storage_control;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddProductActivity extends AppCompatActivity {

    private EditText nameInput, skuInput, categoryInput, priceInput, stockInput, minStockInput, descriptionInput;
    private Button saveButton;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        db = new DatabaseHelper(this);

        initViews();

        saveButton.setOnClickListener(v -> saveProduct());
    }

    private void initViews() {
        nameInput = findViewById(R.id.addName);
        skuInput = findViewById(R.id.addSku);
        categoryInput = findViewById(R.id.addCategory);
        priceInput = findViewById(R.id.addPrice);
        stockInput = findViewById(R.id.addStock);
        minStockInput = findViewById(R.id.addMinStock);
        descriptionInput = findViewById(R.id.addDescription);

        saveButton = findViewById(R.id.addSaveButton);
    }

    private void saveProduct() {
        String name = nameInput.getText().toString().trim();
        String sku = skuInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        String stockStr = stockInput.getText().toString().trim();
        String minStockStr = minStockInput.getText().toString().trim();
        String desc = descriptionInput.getText().toString().trim();

        // Проверка обязательных полей
        if (name.isEmpty() || sku.isEmpty()) {
            Toast.makeText(this, "Введите название и SKU!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Безопасное преобразование цены
        double price = 0;
        if (!TextUtils.isEmpty(priceStr)) {
            try {
                price = Double.parseDouble(priceStr.replace(",", "."));
            } catch (Exception e) {
                Toast.makeText(this, "Введите корректную цену!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Безопасный парсинг количества
        int stock = 0;
        try {
            stock = TextUtils.isEmpty(stockStr) ? 0 : Integer.parseInt(stockStr);
        } catch (Exception e) {
            Toast.makeText(this, "Введите корректное количество!", Toast.LENGTH_SHORT).show();
            return;
        }

        int minStock = 0;
        try {
            minStock = TextUtils.isEmpty(minStockStr) ? 0 : Integer.parseInt(minStockStr);
        } catch (Exception e) {
            Toast.makeText(this, "Введите корректный минимальный остаток!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = db.addProduct(name, sku, price, stock, minStock, category, desc);

        if (ok) {
            Toast.makeText(this, "Товар добавлен", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка при добавлении. Возможно такой SKU уже существует.", Toast.LENGTH_SHORT).show();
        }
    }
}
