package com.internshipbcc.itrip;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.internshipbcc.itrip.Search.ItemSuggestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sena on 15/03/2018.
 */

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DBNAME = "database.sqlite";

    public String path;
    public Context context;

    public DbHelper(Context context) {
        super(context, DBNAME, null, DATABASE_VERSION);
        this.context = context;
        this.path = "/data/data/com.internshipbcc.itrip/databses/" + DBNAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE history(" +
                "title TEXT," +
                "count INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<ItemSuggestion> getHistory() {
        List<ItemSuggestion> items = new ArrayList<>();
        SQLiteDatabase database = getWritableDatabase();
        String query = "Select * from history order by count desc";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                ItemSuggestion history = new ItemSuggestion(cursor.getString(0));
                history.setHistory(true);
                items.add(history);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return items;
    }

    public ArrayList<String> getHistoryTitle() {
        ArrayList<String> items = new ArrayList<>();
        SQLiteDatabase database = getWritableDatabase();
        String query = "Select * from history order by count desc";
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                items.add(cursor.getString(0));

            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return items;
    }

    public boolean addToHistory(String item) {
        SQLiteDatabase database = getWritableDatabase();
        int count = 0;
        Cursor cursor = database.query("history", new String[]{"count"}, "title=?", new String[]{item}, null, null, "count");
        if (cursor.getCount() > 0 && cursor.moveToFirst())
            count = cursor.getInt(0);
        cursor.close();
        ContentValues cv = new ContentValues();
        cv.put("title", item);
        cv.put("count", ++count);
        int success = database.update("history", cv, "title='" + item + "'", null);
        if (success <= 0)
            database.insert("history", null, cv);
        return true;
    }

}
