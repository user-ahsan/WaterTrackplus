package com.ahsan.watertrackplus.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WaterDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "WaterDbHelper";
    private static final String DATABASE_NAME = "WaterTrack.db";
    private static final int DATABASE_VERSION = 1;

    // Table name and columns
    public static final String TABLE_WATER_INTAKE = "water_intake";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DATE = "date";

    // Create table SQL query
    private static final String CREATE_TABLE_WATER_INTAKE = 
        "CREATE TABLE " + TABLE_WATER_INTAKE + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_AMOUNT + " INTEGER NOT NULL,"
        + COLUMN_TIMESTAMP + " INTEGER NOT NULL,"
        + COLUMN_DATE + " TEXT NOT NULL"
        + ")";

    public WaterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WATER_INTAKE);
        Log.d(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATER_INTAKE);
        onCreate(db);
    }

    // Add a new water intake record
    public long addWaterIntake(int amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        long timestamp = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd")
            .format(new java.util.Date(timestamp));

        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_DATE, date);

        long id = db.insert(TABLE_WATER_INTAKE, null, values);
        Log.d(TAG, "New water intake record added: " + amount + "ml");
        return id;
    }

    // Get total water intake for today
    public int getTodayTotalIntake() {
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd")
            .format(new java.util.Date());
        
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_WATER_INTAKE 
            + " WHERE " + COLUMN_DATE + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{date});
        int total = 0;
        
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        
        return total;
    }

    // Get all records for today
    public List<WaterIntakeRecord> getTodayRecords() {
        List<WaterIntakeRecord> records = new ArrayList<>();
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd")
            .format(new java.util.Date());

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_WATER_INTAKE 
            + " WHERE " + COLUMN_DATE + " = ?"
            + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            do {
                WaterIntakeRecord record = new WaterIntakeRecord(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                );
                records.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return records;
    }

    // Delete a record
    public void deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WATER_INTAKE, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        Log.d(TAG, "Record deleted: " + id);
    }

    // Clear all records
    public void clearAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WATER_INTAKE, null, null);
        Log.d(TAG, "All records cleared");
    }
} 