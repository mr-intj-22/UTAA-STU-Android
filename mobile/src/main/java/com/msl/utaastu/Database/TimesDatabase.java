package com.msl.utaastu.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.widget.Toast;

import com.msl.utaastu.CourseSchedule.TimeItem;

import java.util.ArrayList;

import static com.msl.utaastu.Database.TimesDatabase.DatabaseHelper.DAY;
import static com.msl.utaastu.Database.TimesDatabase.DatabaseHelper.TIME;

public class TimesDatabase {

    private SQLiteDatabase mDatabase;

    public TimesDatabase(Context context) {
        DatabaseHelper mHelper = new DatabaseHelper(context);
        mDatabase = mHelper.getWritableDatabase();
    }

    public void addItem(TimeItem data) {
        //create a sql prepared statement
        String sql = "INSERT INTO " + (DatabaseHelper.TABLE_NAME) + " VALUES (?,?);";
        //compile the statement and start a transaction
        SQLiteStatement statement = mDatabase.compileStatement(sql);
        mDatabase.beginTransaction();
        statement.clearBindings();
        //for a given column index, simply bind the data to be put inside that index
        statement.bindString(1, data.getDay());
        statement.bindString(2, data.getTime());

        statement.execute();
        statement.close();
        //set the transaction as successful and end the transaction
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    public void addItems(ArrayList<TimeItem> timeItems, boolean clearPrevious) {
        if (clearPrevious) {
            deleteAllData();
        }
        //create a sql prepared statement
        String sql = "INSERT INTO " + (DatabaseHelper.TABLE_NAME) + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
        //compile the statement and start a transaction
        SQLiteStatement statement = mDatabase.compileStatement(sql);
        mDatabase.beginTransaction();
        for (int i = 0; i < timeItems.size(); i++) {
            TimeItem currentData = timeItems.get(i);
            statement.clearBindings();
            //for a given column index, simply bind the data to be put inside that index
            statement.bindString(1, currentData.getDay());
            statement.bindString(2, currentData.getTime());

            statement.execute();
        }
        statement.close();
        //set the transaction as successful and end the transaction
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    public ArrayList<TimeItem> getAllItems() {
        ArrayList<TimeItem> timeItems = new ArrayList<>();

        //get a list of columns to be retrieved, we need all of them
        String[] columns = {DAY, TIME};

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {

                //create a new movie object and retrieve the data from the cursor to be stored in this movie object
                TimeItem data = new TimeItem();
                //each step is a 2 part process, find the index of the column first, find the data of that column using
                //that index and finally set our blank movie object to contain our data
                data.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
                data.setDay(cursor.getString(cursor.getColumnIndex(DAY)));
                //add the item to the list of data objects which we plan to return
                timeItems.add(data);
            }
            while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed())
            cursor.close();
        return timeItems;
    }

    public boolean isExist(TimeItem item) {
        Cursor cur = mDatabase.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME +
                " WHERE " + DAY + " = '" + item.getDay() +
                " AND " + TIME + " = " + item.getTime() + "'", null);
        boolean exist = (cur.getCount() > 0);
        cur.close();
        return exist;
    }

    public void deleteAllData() {
        mDatabase.delete(DatabaseHelper.TABLE_NAME, null, null);
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        public static final String TABLE_NAME = "time_table";
        public static final String DAY = "day";
        public static final String TIME = "time";
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                DAY + " TEXT," +
                TIME + " TEXT" +
                ");";
        private static final String DB_NAME = "TIMETABLE";
        private static final int DB_VERSION = 1;
        private Context mContext;

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE);
            } catch (SQLiteException exception) {
                Toast.makeText(mContext, exception + "", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(" DROP TABLE " + TABLE_NAME + " IF EXISTS;");
                onCreate(db);
            } catch (SQLiteException exception) {
                Toast.makeText(mContext, exception + "", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
