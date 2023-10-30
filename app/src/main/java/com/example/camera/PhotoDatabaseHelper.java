package com.example.camera;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PhotoDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "photo_database";
    private static final int DATABASE_VERSION = 1;

    public PhotoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS photos (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "photo_path TEXT," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "latitude REAL," +
                "longitude REAL)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果需要更新数据库结构，可以在这里进行处理
    }
}

