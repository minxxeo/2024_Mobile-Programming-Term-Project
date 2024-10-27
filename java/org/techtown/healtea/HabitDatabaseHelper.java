package org.techtown.healtea;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HabitDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "habits.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "habits";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_HABIT = "habit";
    private static final String COLUMN_COLOR = "color";
    private static final String COLUMN_IS_CHECKED = "is_checked";

    public HabitDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_HABIT + " TEXT,"
                + COLUMN_COLOR + " INTEGER,"
                + COLUMN_IS_CHECKED + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public int addHabit(String date, String habit, int color, int isChecked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_HABIT, habit);
        values.put(COLUMN_COLOR, color);
        values.put(COLUMN_IS_CHECKED, isChecked);

        // Insert the new row, returning the primary key value of the new row
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return (int) id;  // Return the id as an integer
    }

    // Example methods in HabitDatabaseHelper.java
    public Cursor getHabits(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("habits", null, "date = ?", new String[]{date}, null, null, null);
    }

    public void updateHabit(int id, String habit, int color, int isChecked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("habit", habit);
        values.put("color", color);
        values.put("is_checked", isChecked);
        db.update("habits", values, "_id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteHabit(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}

