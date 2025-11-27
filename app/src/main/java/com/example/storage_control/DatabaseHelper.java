package com.example.storage_control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "OrderManager.db";
    private static final int DATABASE_VERSION = 5; // Увеличиваем версию!

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Таблица заказов
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_CUSTOMER_NAME = "customer_name";
    private static final String COLUMN_CUSTOMER_PHONE = "customer_phone";
    private static final String COLUMN_CUSTOMER_ADDRESS = "customer_address";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_ORDER_ITEMS = "order_items";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_DELIVERY_DATE = "delivery_date";
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_PRODUCT_NAME = "product_name";
    private static final String COLUMN_PRODUCT_SKU = "sku";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_STOCK_QUANTITY = "stock_quantity";
    private static final String COLUMN_MIN_STOCK = "min_stock";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DESCRIPTION = "description";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Создание новой базы данных");

        // Создаем таблицу пользователей
        String createUserTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + "role TEXT"
                + ")";
        db.execSQL(createUserTable);
        addDefaultUsers(db);
        Log.d("DatabaseHelper", "Таблица users создана");

        // Создаем таблицу заказов с новыми полями
        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_ID + " TEXT,"
                + COLUMN_CUSTOMER_NAME + " TEXT,"
                + COLUMN_CUSTOMER_PHONE + " TEXT,"
                + COLUMN_CUSTOMER_ADDRESS + " TEXT,"
                + COLUMN_AMOUNT + " REAL,"
                + COLUMN_STATUS + " TEXT,"
                + COLUMN_ORDER_ITEMS + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_DELIVERY_DATE + " TEXT"
                + ")";

        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PRODUCT_NAME + " TEXT,"
                + COLUMN_PRODUCT_SKU + " TEXT UNIQUE,"
                + COLUMN_PRICE + " REAL,"
                + COLUMN_STOCK_QUANTITY + " INTEGER,"
                + COLUMN_MIN_STOCK + " INTEGER,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT"
                + ")";
        db.execSQL(createProductsTable);

        db.execSQL(createOrdersTable);
        Log.d("DatabaseHelper", "Таблица orders создана");

        // Добавляем тестовые данные с новыми полями
        insertSampleOrders(db);
        Log.d("DatabaseHelper", "Тестовые данные добавлены");
        insertSampleProducts(db);
    }

    private void addDefaultUsers(SQLiteDatabase db) {
        insertUserIfNotExists(db, "Order Manager", "order@mail.com", "test", "orders_manager");
        insertUserIfNotExists(db, "Product Manager", "product@mail.com", "test", "products_manager");
        insertUserIfNotExists(db, "Analytics Manager", "analytic@mail.com", "test", "analytics_manager");
    }

    // Регистрация пользователя с ролью
    public boolean registerUserWithRole(String name, String email, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put("role", role);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Получить сумму выполненных заказов
    public double getCompletedOrdersRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_ORDERS + " WHERE " + COLUMN_STATUS + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{"delivered"});

        double revenue = 0;
        if (cursor.moveToFirst()) {
            revenue = cursor.getDouble(0);
        }
        cursor.close();
        return revenue;
    }

    // Получить доходы по месяцам
    public Map<String, Double> getMonthlyRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();

        // Запрос для группировки по месяцам
        String query = "SELECT strftime('%Y-%m', " + COLUMN_DATE + ") as month, " +
                "SUM(" + COLUMN_AMOUNT + ") as revenue " +
                "FROM " + TABLE_ORDERS + " " +
                "WHERE " + COLUMN_STATUS + " = 'delivered' " +
                "GROUP BY month " +
                "ORDER BY month DESC " +
                "LIMIT 6"; // Последние 6 месяцев

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String month = cursor.getString(cursor.getColumnIndexOrThrow("month"));
                double revenue = cursor.getDouble(cursor.getColumnIndexOrThrow("revenue"));

                // Форматируем название месяца
                String formattedMonth = formatMonth(month);
                monthlyRevenue.put(formattedMonth, revenue);

            } while (cursor.moveToNext());
        }
        cursor.close();

        return monthlyRevenue;
    }

    // Форматирование названия месяца
    private String formatMonth(String month) {
        try {
            String[] parts = month.split("-");
            int year = Integer.parseInt(parts[0]);
            int monthNum = Integer.parseInt(parts[1]);

            String[] monthNames = {"Янв", "Фев", "Мар", "Апр", "Май", "Июн",
                    "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};

            return monthNames[monthNum - 1] + " " + year;
        } catch (Exception e) {
            return month;
        }
    }

    private void insertUserIfNotExists(SQLiteDatabase db, String name, String email, String password, String role) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?",
                new String[]{email}
        );

        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_PASSWORD, password);
            values.put("role", role);

            db.insert(TABLE_USERS, null, values);
        }
    }

    // Получить данные пользователя по email
    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_NAME, COLUMN_EMAIL, "role"};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        return db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
    }


    private void insertSampleOrders(SQLiteDatabase db) {
        // Тестовые заказы с полной информацией
        addOrder(db, "ORD-2876", "Иванов А.", "+7 (999) 111-22-33",
                "г. Москва, ул. Ленина, д. 10, кв. 5", 14290, "new",
                "Ноутбук ASUS - 1 шт.\nМышь беспроводная - 1 шт.",
                "2024-01-15", "2024-01-20");


        addOrder(db, "ORD-2875", "Петрова Е.", "+7 (999) 444-55-66",
                "г. Москва, пр. Мира, д. 25, кв. 12", 8560, "processing",
                "Смартфон Samsung - 1 шт.\nЧехол - 1 шт.",
                "2024-01-14", "2024-01-18");

        addOrder(db, "ORD-2874", "Сидоров В.", "+7 (999) 777-88-99",
                "г. Москва, ул. Пушкина, д. 15, кв. 8", 21350, "delivered",
                "Телевизор LG - 1 шт.\nКронштейн - 1 шт.",
                "2024-01-13", "2024-01-16");

        addOrder(db, "ORD-2874", "Сидоров В.", "+7 (999) 777-88-99",
                "г. Москва, ул. Пушкина, д. 15, кв. 8", 11350, "delivered",
                "Телевизор LG - 1 шт.\nКронштейн - 1 шт.",
                "2024-02-13", "2024-02-16");

        addOrder(db, "ORD-2874", "Сидоров В.", "+7 (999) 777-88-99",
                "г. Москва, ул. Пушкина, д. 15, кв. 8", 25350, "delivered",
                "Телевизор LG - 1 шт.\nКронштейн - 1 шт.",
                "2024-03-13", "2024-03-16");

        addOrder(db, "ORD-2873", "Козлов Д.", "+7 (999) 123-45-67",
                "г. Москва, ул. Гагарина, д. 7, кв. 3", 12400, "overdue",
                "Планшет iPad - 1 шт.\nСтилус - 1 шт.",
                "2024-01-10", "2024-01-15");

        addOrder(db, "ORD-2872", "Николаева С.", "+7 (999) 987-65-43",
                "г. Москва, ул. Садовая, д. 18, кв. 9", 7800, "overdue",
                "Наушники Sony - 1 шт.\nКабель USB-C - 1 шт.",
                "2024-01-09", "2024-01-14");
    }

    private void addOrder(SQLiteDatabase db, String orderId, String customer, String phone,
                          String address, double amount, String status, String items,
                          String date, String deliveryDate) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_ID, orderId);
        values.put(COLUMN_CUSTOMER_NAME, customer);
        values.put(COLUMN_CUSTOMER_PHONE, phone);
        values.put(COLUMN_CUSTOMER_ADDRESS, address);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_ORDER_ITEMS, items);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DELIVERY_DATE, deliveryDate);
        db.insert(TABLE_ORDERS, null, values);
    }

    private void insertSampleProducts(SQLiteDatabase db) {
        addProduct(db, "Ноутбук ASUS VivoBook", "NB-ASUS-001", 45000, 15, 5, "Электроника", "15.6 дюймов, Intel i5, 8GB RAM");
        addProduct(db, "Смартфон Samsung Galaxy", "PH-SSG-002", 35000, 8, 3, "Электроника", "6.1 дюймов, 128GB");
        addProduct(db, "Телевизор LG 55\"", "TV-LG-055", 65000, 6, 2, "Электроника", "4K UHD, Smart TV");
        addProduct(db, "Наушники Sony WH-1000XM4", "HP-SONY-XM4", 25000, 20, 10, "Аксессуары", "Беспроводные, шумоподавление");
        addProduct(db, "Мышь беспроводная Logitech", "MS-LOGI-MX", 4500, 25, 8, "Аксессуары", "Беспроводная, оптическая");
        addProduct(db, "Клавиатура механическая", "KB-MECH-RGB", 12000, 12, 4, "Аксессуары", "RGB подсветка, механическая");
    }

    private void addProduct(SQLiteDatabase db, String name, String sku, double price,
                            int stock, int minStock, String category, String description) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_SKU, sku);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_STOCK_QUANTITY, stock);
        values.put(COLUMN_MIN_STOCK, minStock);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DESCRIPTION, description);
        db.insert(TABLE_PRODUCTS, null, values);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Добавляем удаление таблицы товаров
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("ALTER TABLE users ADD COLUMN role TEXT DEFAULT 'user'");
        onCreate(db);
    }

    // ==================== МЕТОДЫ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ====================

    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }




    // ==================== МЕТОДЫ ДЛЯ ЗАКАЗОВ ====================

    // Получить ВСЕ заказы
    public Cursor getAllOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ORDERS, null, null, null, null, null, COLUMN_ID + " DESC");
    }

    // Получить заказы по статусу
    public Cursor getOrdersByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_STATUS + " = ?";
        String[] selectionArgs = {status};
        return db.query(TABLE_ORDERS, null, selection, selectionArgs, null, null, COLUMN_ID + " DESC");
    }

    // Получить количество заказов по статусу
    public int getOrdersCountByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE " + COLUMN_STATUS + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{status});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Получить последние N заказов
    public Cursor getRecentOrders(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ORDERS, null, null, null, null, null, COLUMN_ID + " DESC", String.valueOf(limit));
    }

    // Получить заказ по ID
    public Cursor getOrderById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return db.query(TABLE_ORDERS, null, selection, selectionArgs, null, null, null);
    }

    // Обновить заказ
    public boolean updateOrder(int id, String customerName, String phone, String address,
                               double amount, String status, String items, String deliveryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, customerName);
        values.put(COLUMN_CUSTOMER_PHONE, phone);
        values.put(COLUMN_CUSTOMER_ADDRESS, address);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_ORDER_ITEMS, items);
        values.put(COLUMN_DELIVERY_DATE, deliveryDate);

        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        int result = db.update(TABLE_ORDERS, values, whereClause, whereArgs);
        return result > 0;
    }



    // ==================== МЕТОДЫ ДЛЯ ТОВАРОВ ====================

    // Получить все товары
    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTS, null, null, null, null, null, COLUMN_PRODUCT_NAME + " ASC");
    }

    // Получить товары с критическим остатком
    public Cursor getCriticalStockProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_STOCK_QUANTITY + " <= " + COLUMN_MIN_STOCK;
        return db.query(TABLE_PRODUCTS, null, selection, null, null, null,
                COLUMN_STOCK_QUANTITY + " ASC");
    }


    // Получить количество товаров с критическим остатком
    public int getCriticalStockCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_PRODUCTS + " WHERE " +
                COLUMN_STOCK_QUANTITY + " <= " + COLUMN_MIN_STOCK;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Добавить новый товар
    public boolean addProduct(String name, String sku, double price, int stock,
                              int minStock, String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_SKU, sku);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_STOCK_QUANTITY, stock);
        values.put(COLUMN_MIN_STOCK, minStock);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DESCRIPTION, description);

        long result = db.insert(TABLE_PRODUCTS, null, values);
        return result != -1;
    }

    // Обновить товар
    public boolean updateProduct(int id, String name, String sku, double price,
                                 int stock, int minStock, String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_SKU, sku);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_STOCK_QUANTITY, stock);
        values.put(COLUMN_MIN_STOCK, minStock);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DESCRIPTION, description);

        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        int result = db.update(TABLE_PRODUCTS, values, whereClause, whereArgs);
        return result > 0;
    }

    // Удалить товар
    public boolean deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        int result = db.delete(TABLE_PRODUCTS, whereClause, whereArgs);
        return result > 0;
    }

    // Получить товар по ID
    public Cursor getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return db.query(TABLE_PRODUCTS, null, selection, selectionArgs, null, null, null);
    }

    public int getLowStockCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_PRODUCTS +
                " WHERE " + COLUMN_STOCK_QUANTITY + " > 0 AND " +
                COLUMN_STOCK_QUANTITY + " <= " + COLUMN_MIN_STOCK;

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }


    // Получить общее количество товаров
    public int getTotalProductsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_PRODUCTS;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Получить товары с низким остатком
    public Cursor getLowStockProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_STOCK_QUANTITY + " > 0 AND " +
                COLUMN_STOCK_QUANTITY + " <= " + COLUMN_MIN_STOCK;

        return db.query(TABLE_PRODUCTS, null, selection, null, null, null,
                COLUMN_STOCK_QUANTITY + " ASC");
    }


    public Cursor getProductBySku(String sku) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTS, null, "sku = ?", new String[]{sku}, null, null, null);
    }

    public boolean updateProduct(String sku, String name, double price, int stock, int minStock, String category, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("product_name", name);
        values.put("price", price);
        values.put("stock_quantity", stock);
        values.put("min_stock", minStock);
        values.put("category", category);
        values.put("description", description);

        int rowsAffected = db.update(TABLE_PRODUCTS, values, "sku = ?", new String[]{sku});
        return rowsAffected > 0;
    }

    public boolean deleteProduct(String sku) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PRODUCTS, "sku = ?", new String[]{sku});
        return rowsAffected > 0;
    }


    // ==================== ОТЛАДОЧНЫЕ МЕТОДЫ ====================

    public String getAllUsersForDebug() {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder result = new StringBuilder();

        Cursor cursor = db.rawQuery("SELECT * FROM users", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));

                result.append("ID: ").append(id)
                        .append(", Name: ").append(name)
                        .append(", Email: ").append(email)
                        .append(", Password: ").append(password)
                        .append("\n");
            } while (cursor.moveToNext());
        }
        cursor.close();

        return result.toString();
    }

    // Метод для получения пути к БД (для отладки)
    public String getDatabasePath() {
        return getReadableDatabase().getPath();
    }

    public int getOrdersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM orders", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getProductsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM products", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getCriticalProductsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_PRODUCTS +
                " WHERE " + COLUMN_STOCK_QUANTITY + " <= " + COLUMN_MIN_STOCK;
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }



}