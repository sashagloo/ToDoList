package com.example.sasha.todolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sasha on 23-9-2015.
 */
public class DBOpenHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for identifying table and columns
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_CREATED = "noteCreated";
    public static final String NOTE_PRIORITY = "notePriority";
    public static final String NOTE_DEADLINE = "noteDeadline";

    public static final String[] ALL_COLUMNS =
            { NOTE_ID, NOTE_TEXT, NOTE_CREATED, NOTE_PRIORITY, NOTE_DEADLINE };

    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOTE_TEXT + " TEXT, " +
                    NOTE_CREATED + " DATETIME default CURRENT_TIMESTAMP, " +
                    NOTE_PRIORITY + " INTEGER, " +
                    NOTE_DEADLINE + " DATE" +
                    ")";

    /**
     * @param context to use to open or create the database
     * @param DATABASE_NAME  of the database file, or null for an in-memory database
     * @param factory  null for the default (or to use for creating cursor objects)
     * @param DATABASE_VERSION number of the database (starting at 1); if the database is older,
     *                {@link #onUpgrade} will be used to upgrade the database; if the database is
     *                newer, {@link #onDowngrade} will be used to downgrade the database
     */
    public DBOpenHelper(Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    /**
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }
}
