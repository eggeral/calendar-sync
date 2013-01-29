package com.axegr.calendarsync;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class CalendarSyncContract {

	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
			+ CalendarSyncEntry.TABLE_NAME + " ("
			+ CalendarSyncEntry._ID
			+ " INTEGER PRIMARY KEY,"
			+ CalendarSyncEntry.COLUMN_NAME_ORIGINAL_CALENDAR_ID
			+ INTEGER_TYPE + COMMA_SEP
			+ CalendarSyncEntry.COLUMN_NAME_ORIGINAL_ENTRY_ID
			+ INTEGER_TYPE + COMMA_SEP
			+ CalendarSyncEntry.COLUMN_NAME_TARGET_CALENDAR_ID
			+ INTEGER_TYPE + COMMA_SEP
			+ CalendarSyncEntry.COLUMN_NAME_TARGET_ENTRY_ID
			+ INTEGER_TYPE 
			+  " )";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ CalendarSyncEntry.TABLE_NAME;

	private CalendarSyncContract() {
	};

	public static abstract class CalendarSyncEntry implements BaseColumns {
		public static final String TABLE_NAME = "calendarsyncentry";
		public static final String COLUMN_NAME_ORIGINAL_CALENDAR_ID = "original_calendar_id";
		public static final String COLUMN_NAME_ORIGINAL_ENTRY_ID = "original_entry_id";
		public static final String COLUMN_NAME_TARGET_CALENDAR_ID = "target_calendar_id";
		public static final String COLUMN_NAME_TARGET_ENTRY_ID = "target_entry_id";
	}

	public static class CalendarSyncDbHelper extends SQLiteOpenHelper {
	    // If you change the database schema, you must increment the database version.
	    public static final int DATABASE_VERSION = 1;
	    public static final String DATABASE_NAME = "FeedReader.db";

	    public CalendarSyncDbHelper (Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(SQL_CREATE_ENTRIES);
	    }
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        db.execSQL(SQL_DELETE_ENTRIES);
	        onCreate(db);
	    }
	    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        onUpgrade(db, oldVersion, newVersion);
	    }
	}

}
