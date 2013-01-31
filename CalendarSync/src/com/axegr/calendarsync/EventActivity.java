package com.axegr.calendarsync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class EventActivity extends Activity {

	private static final String TAG = "calsync";

	private String eventIdA;
	private String eventIdB;
	
	private String calendarIdA;
	private String calendarIdB;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event);

		CalendarSyncContract.CalendarSyncDbHelper mDbHelper = new CalendarSyncContract.CalendarSyncDbHelper(
				this);
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();

		eventIdA = getIntent().getStringExtra("idA");
		eventIdB = getIntent().getStringExtra("idB");
		calendarIdA = getIntent().getStringExtra("calenderIdA");
		calendarIdB = getIntent().getStringExtra("calenderIdB");

		final Cursor eventA = refreshEvents();

		Button addButton = (Button) findViewById(R.id.add_button);
		addButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Add " + calendarIdA + ", target: " + calendarIdB);
				ContentResolver cr = getContentResolver();
				ContentValues values = new ContentValues();
				copyColumn(eventA, values, Events.DTSTART);
				copyColumn(eventA, values, Events.DTEND);
				copyColumn(eventA, values, Events.DURATION);
				copyColumn(eventA, values, Events.TITLE);
				copyColumn(eventA, values, Events.DESCRIPTION);
				copyColumn(eventA, values, Events.EVENT_TIMEZONE);
				copyColumn(eventA, values, Events.RDATE);
				copyColumn(eventA, values, Events.RRULE);

				values.put(Events.CALENDAR_ID, calendarIdB);
				Uri uri = cr.insert(Events.CONTENT_URI, values);
				long eventId = Long.parseLong(uri.getLastPathSegment());

				ContentValues dbValues = new ContentValues();
				dbValues.put(
						CalendarSyncContract.CalendarSyncEntry.COLUMN_NAME_ORIGINAL_CALENDAR_ID,
						calendarIdA);
				dbValues.put(
						CalendarSyncContract.CalendarSyncEntry.COLUMN_NAME_ORIGINAL_ENTRY_ID,
						values.getAsInteger(Events._ID));
				dbValues.put(
						CalendarSyncContract.CalendarSyncEntry.COLUMN_NAME_TARGET_CALENDAR_ID,
						calendarIdB);
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
				Log.i(TAG, "Delete " + calendarIdA + ", event: " + eventIdA);
				Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI,
						Long.parseLong(eventIdA));
				int rowsDeleted = getContentResolver().delete(deleteUri, null,
						null);
				Log.i(TAG, "Deleted " + rowsDeleted + "rows.");
				finish();
			}

		});

		Button findButton = (Button) findViewById(R.id.find_button);
		findButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String eventId = eventIdA; // TODO: select eventIdA or eventIdA.
											// Whatever is not null.

				Cursor originalEvent = eventA;
				String originalCalendar = calendarIdA;
				String targetCalendar = calendarIdB;

				Log.i(TAG, "Find match for " + originalCalendar + ", event: "
						+ eventId + " in calendar " + targetCalendar);

				Uri uri = Events.CONTENT_URI;
				String selection = Events.TITLE + " = ? AND " + Events.DTSTART
						+ " = ? AND " + Events.CALENDAR_ID + " = ?";
				String[] selectionArgs = new String[] {
						originalEvent.getString(originalEvent
								.getColumnIndex(Events.TITLE)),
						originalEvent.getString(originalEvent
								.getColumnIndex(Events.DTSTART)),
						targetCalendar };
				ContentResolver cr = getContentResolver();
				final Cursor cur = cr.query(uri, null, selection,
						selectionArgs, null);
				cur.moveToFirst();
				if (cur.isAfterLast()) {
					Log.i(TAG, "No match found!");
				} else {
					Log.i(TAG,
							"Found: "
									+ cur.getString(cur
											.getColumnIndex(Events._ID))
									+ ", "
									+ cur.getString(cur
											.getColumnIndex(Events.TITLE)));
					eventIdB = cur.getString(cur
											.getColumnIndex(Events._ID));
					refreshEvents();
				}
			}

		});
	}

	private Cursor refreshEvents() {
		final Cursor eventA = getEventCursor(eventIdA);
		final Cursor eventB = getEventCursor(eventIdB);

		List<String> propertyNames = getPropertyNames(eventA, eventB);

		List<Map<String, String>> values = new ArrayList<Map<String, String>>();

		for (String propertyName : propertyNames) {
			Map<String, String> property = new HashMap<String, String>();
			property.put("name", propertyName);
			property.put(
					"valueA",
					eventA == null ? "" : eventA.getString(eventA
							.getColumnIndex(propertyName)));
			property.put(
					"valueB",
					eventB == null ? "" : eventB.getString(eventB
							.getColumnIndex(propertyName)));
			values.add(property);
		}

		String[] from = new String[] { "name", "valueA", "valueB" };
		int[] to = new int[] { R.id.event_prop_name, R.id.event_prop_value_a,
				R.id.event_prop_value_b };

		SimpleAdapter adapterA = new SimpleAdapter(this, values,
				R.layout.event_properties_item, from, to);

		ListView listView = (ListView) findViewById(R.id.event_properties);
		listView.setAdapter(adapterA);
		return eventA;
	}

	private List<String> getPropertyNames(final Cursor eventA,
			final Cursor eventB) {
		List<String> propertyNames = new ArrayList<String>();
		Cursor cur = null;
		if (eventA != null) {
			cur = eventA;
		} else if (eventB != null) {
			cur = eventB;
		}
		if (cur != null) {
			for (int i = 0; i < cur.getColumnCount(); i++) {
				propertyNames.add(cur.getColumnName(i));
			}
		}
		Collections.sort(propertyNames);
		return propertyNames;
	}

	private Cursor getEventCursor(final String eventId) {
		if (eventId == null) {
			return null;
		}
		ContentResolver cr = getContentResolver();
		Uri uri = Events.CONTENT_URI;
		String selection = Events._ID + " = ?";
		String[] selectionArgs = new String[] { eventId };
		final Cursor cur = cr.query(uri, null, selection, selectionArgs, null);
		cur.moveToFirst();
		return cur;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_event, menu);
		return true;
	}

}
