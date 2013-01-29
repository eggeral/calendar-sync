package com.axegr.calendarsync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class EventActivity extends Activity {

	private static final String TAG = "calsync";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event);

		CalendarSyncContract.CalendarSyncDbHelper mDbHelper = new CalendarSyncContract.CalendarSyncDbHelper(
				this);
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();

		final long eventIdA = getIntent().getLongExtra("event_id", -1);
		Cursor query = db
				.query(CalendarSyncContract.CalendarSyncEntry.TABLE_NAME,
						new String[] { CalendarSyncContract.CalendarSyncEntry.COLUMN_NAME_ORIGINAL_ENTRY_ID },
						CalendarSyncContract.CalendarSyncEntry.COLUMN_NAME_TARGET_ENTRY_ID
								+ "=?",
						new String[] { Long.toString(eventIdA) }, null, null,
						null);
		if (query.moveToFirst()) {
			long eventIdB = query.getLong(0);
			displayEvent(eventIdB, (ListView) findViewById(R.id.event_b));
		}

		final String calendarId = getIntent().getStringExtra("calendar_id");
		final String targetCalendarId = getIntent().getStringExtra(
				"target_calendar_id");
		final Cursor curA = displayEvent(eventIdA,
				(ListView) findViewById(R.id.event_a));

		Button addButton = (Button) findViewById(R.id.add_button);
		addButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Add " + calendarId + ", target: "
						+ targetCalendarId);
				ContentResolver cr = getContentResolver();
				ContentValues values = new ContentValues();
				copyColumn(curA, values, Events.DTSTART);
				copyColumn(curA, values, Events.DTEND);
				copyColumn(curA, values, Events.DURATION);
				copyColumn(curA, values, Events.TITLE);
				copyColumn(curA, values, Events.DESCRIPTION);
				copyColumn(curA, values, Events.EVENT_TIMEZONE);
				copyColumn(curA, values, Events.RDATE);
				copyColumn(curA, values, Events.RRULE);

				values.put(Events.CALENDAR_ID, targetCalendarId);
				Uri uri = cr.insert(Events.CONTENT_URI, values);
				long eventId = Long.parseLong(uri.getLastPathSegment());

				ContentValues dbValues = new ContentValues();
				dbValues.put(
						CalendarSyncContract.CalendarSyncEntry.COLUMN_NAME_ORIGINAL_CALENDAR_ID,
						calendarId);
				dbValues.put(
						CalendarSyncContract.CalendarSyncEntry.COLUMN_NAME_ORIGINAL_ENTRY_ID,
						values.getAsInteger(Events._ID));
				dbValues.put(
						CalendarSyncContract.CalendarSyncEntry.COLUMN_NAME_TARGET_CALENDAR_ID,
						targetCalendarId);
				dbValues.put(
						CalendarSyncContract.CalendarSyncEntry.COLUMN_NAME_TARGET_ENTRY_ID,
						eventId);

				long newRowId;
				newRowId = db.insert(
						CalendarSyncContract.CalendarSyncEntry.TABLE_NAME,
						null, dbValues);

			}

			private void copyColumn(final Cursor cur, ContentValues values,
					String column) {
				values.put(column, cur.getString(cur.getColumnIndex(column)));
			}
		});

		Button deleteButton = (Button) findViewById(R.id.delete_button);
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Delete " + calendarId + ", event: " + eventIdA);
				Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI,
						eventIdA);
				int rowsDeleted = getContentResolver().delete(deleteUri, null,
						null);
				Log.i(TAG, "Deleted " + rowsDeleted + "rows.");
				finish();
			}

		});

	}

	private Cursor displayEvent(final long eventId, ListView listView) {
		ContentResolver cr = getContentResolver();
		Uri uri = Events.CONTENT_URI;
		String selection = Events._ID + " = ?";
		String[] selectionArgs = new String[] { Long.toString(eventId) };
		final Cursor cur = cr.query(uri, null, selection, selectionArgs, null);
		cur.moveToFirst();

		List<Map<String, String>> valuesA = new ArrayList<Map<String, String>>();
		for (int i = 0; i < cur.getColumnCount(); i++) {
			Map<String, String> property = new HashMap<String, String>();
			property.put("name", cur.getColumnName(i));
			property.put("value", cur.getString(i));
			valuesA.add(property);
		}

		Collections.sort(valuesA, new Comparator<Map<String, String>>() {

			@Override
			public int compare(Map<String, String> lhs, Map<String, String> rhs) {
				return lhs.get("name").compareTo(rhs.get("name"));
			}

		});

		for (Map<String, String> value : valuesA) {
			Log.i(TAG, value.get("name") + " - " + value.get("value"));
		}

		String[] from = new String[] { "name", "value" };
		int[] to = new int[] { R.id.event_item1, R.id.event_item2 };

		SimpleAdapter adapterA = new SimpleAdapter(this, valuesA,
				R.layout.event_properties_item, from, to);

		listView.setAdapter(adapterA);
		return cur;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_event, menu);
		return true;
	}

}
