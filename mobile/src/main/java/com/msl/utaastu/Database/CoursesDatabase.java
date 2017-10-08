package com.msl.utaastu.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import static com.msl.utaastu.Database.DepartmentsDatabase.DatabaseHandler.STRING;


public class CoursesDatabase {

    private SQLiteDatabase mDatabase;

    public CoursesDatabase(Context context) {
        DatabaseHandler mHelper = new DatabaseHandler(context);
        mDatabase = mHelper.getWritableDatabase();
    }

    public void addString(String string) {
        SQLiteDatabase db = mDatabase;

        ContentValues values = new ContentValues();
        values.put(STRING, string); // Contact Name

        // Inserting Row
        db.insert(DatabaseHandler.TABLE_NAME, null, values);
    }

    public boolean isExist(String string) {
        Cursor cur = mDatabase.rawQuery("SELECT * FROM " + DatabaseHandler.TABLE_NAME + " WHERE " + STRING + " = '" + string + "'", null);
        boolean exist = (cur.getCount() > 0);
        cur.close();
        return exist;
    }

    public List<String> getAllStrings() {
        List<String> strings = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DatabaseHandler.TABLE_NAME;

        SQLiteDatabase db = mDatabase;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                strings.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        if (!cursor.isClosed())
            cursor.close();
        // return id list
        return strings;
    }

    public void deleteAllData() {
        mDatabase.delete(DatabaseHandler.TABLE_NAME, null, null);
    }

    static class DatabaseHandler extends SQLiteOpenHelper {

        // All Static variables
        // Database Version
        static final int DATABASE_VERSION = 1;

        // Database Name
        static final String DATABASE_NAME = "Courses";

        // Contacts table name
        static final String TABLE_NAME = "courses";

        // Contacts Table Columns names
        static final String KEY_ID = "id";
        static final String STRING = "string";

        private DatabaseHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Creating Tables
        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY," + STRING + " TEXT" + ")";
            db.execSQL(CREATE_CONTACTS_TABLE);
        }

        // Upgrading database
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            // Create tables again
            onCreate(db);
        }
    }

}