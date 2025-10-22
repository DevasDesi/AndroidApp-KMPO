package com.example.storage_control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "OrderManager.db";
    private static final int DATABASE_VERSION = 2;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_CUSTOMER_NAME = "customer_name";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT"
                + ")";
        db.execSQL(createUserTable);

        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_ID + " TEXT,"
                + COLUMN_CUSTOMER_NAME + " TEXT,"
                + COLUMN_AMOUNT + " REAL,"
                + COLUMN_STATUS + " TEXT,"
                + COLUMN_DATE + " TEXT"
                + ")";
        db.execSQL(createOrdersTable);
        insertSampleOrders(db);
    }

    private void insertSampleOrders(SQLiteDatabase db) {
        // Тестовые заказы
        addOrder(db, "ORD-2876", "Иванов А.", 14290, "new", "2024-01-15");
        addOrder(db, "ORD-2875", "Петрова Е.", 8560, "processing", "2024-01-14");
        addOrder(db, "ORD-2874", "Сидоров В.", 21350, "delivered", "2024-01-13");
        addOrder(db, "ORD-2873", "Козлов Д.", 12400, "overdue", "2024-01-10");
        addOrder(db, "ORD-2872", "Николаева С.", 7800, "overdue", "2024-01-09");
    }

    private void addOrder(SQLiteDatabase db, String orderId, String customer, double amount, String status, String date) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_ID, orderId);
        values.put(COLUMN_CUSTOMER_NAME, customer);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_DATE, date);
        db.insert(TABLE_ORDERS, null, values);
    }

    public Cursor getAllOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ORDERS, null, null, null, null, null, COLUMN_ID + " DESC");
    }

    public Cursor getOrdersByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_STATUS + " = ?";
        String[] selectionArgs = {status};
        return db.query(TABLE_ORDERS, null, selection, selectionArgs, null, null, COLUMN_ID + " DESC");
    }

    public int getOrdersCountByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE " + COLUMN_STATUS + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{status});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public Cursor getRecentOrders(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ORDERS, null, null, null, null, null, COLUMN_ID + " DESC", String.valueOf(limit));
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаляем ВСЕ таблицы при обновлении БД
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);

        // Создаем таблицы заново
        onCreate(db);
    }



    // Регистрация пользователя
    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Проверка логина
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

    // Проверка существования email
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
}