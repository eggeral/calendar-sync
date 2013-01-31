package com.axegr.calendarsync;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String[] CAL_PROJECTION = new String[] { Calendars._ID,
			Calendars.NAME, Calendars.ACCOUNT_NAME };

	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] { Events._ID,
			Events._SYNC_ID, Events.TITLE, Events.DTSTART, };

	private static final int CAL_PROJECTION_ID_INDEX = 0;
	private static final int CAL_PROJECTION_NAME = 1;
	private static final int CAL_PROJECTION_LOCATION = 2;

	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_SYNC_ID = 1;
	private static final int PROJECTION_TITLE = 2;
	private static final int PROJECTION_DTSTART = 3;

	private static final String TAG = "calsync";

	private static final String calenderAId = "1";
	private static final String calenderBId = "11";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button refreshButton = (Button) findViewById(R.id.button_refresh);
		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refreshEvents();
			}
		});
		refreshEvents();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		refreshEvents();
	}

	private void refreshEvents() {
		printCalendarsToLog();

		setCalendarName(calenderAId, (TextView) findViewById(R.id.text_cal_a));
		setCalendarName(calenderBId, (TextView) findViewById(R.id.text_cal_b));

		Uri uri = Events.CONTENT_URI;
		String selection = Events.CALENDAR_ID
				+ " = ? AND original_id is null AND deleted = 0";
		String order = Events.DTSTART;
		String[] selectionArgsA = new String[] { calenderAId };
		Cursor curA = getContentResolver().query(uri, EVENT_PROJECTION,
				selection, selectionArgsA, order);

		String[] selectionArgsB = new String[] { calenderBId };
		Cursor curB = getContentResolver().query(uri, EVENT_PROJECTION,
				selection, selectionArgsB, order);

		curA.moveToNext();
		curB.moveToNext();
		long dtStartA = -1;
		long dtStartB = -1;

		List<Map<String, String>> listEntries = new ArrayList<Map<String, String>>();

		while (!curA.isAfterLast() && !curB.isAfterLast()) {
			Map<String, String> entry = new HashMap<String, String>();
			if (!curA.isAfterLast()) {
				dtStartA = curA.getLong(PROJECTION_DTSTART);
			}

			if (!curB.isAfterLast()) {
				dtStartB = curB.getLong(PROJECTION_DTSTART);
			}

			if (dtStartA > dtStartB) {
				Log.i(TAG, "B: " + dtStartB);
				entry.put("idB", curB.getString(PROJECTION_ID_INDEX));
				entry.put("titleB", curB.getString(PROJECTION_TITLE));
				entry.put("dtStartB", curB.getString(PROJECTION_DTSTART));
				curB.moveToNext();
			} else {
				Log.i(TAG, "A: " + dtStartA);
				entry.put("idA", curA.getString(PROJECTION_ID_INDEX));
				entry.put("titleA", curA.getString(PROJECTION_TITLE));
				entry.put("dtStartA", curA.getString(PROJECTION_DTSTART));
				curA.moveToNext();
			}
			listEntries.add(entry);
		}

		ListView listViewA = (ListView) findViewById(R.id.eventlist);
		String[] from = new String[] { "idA", "titleA", "dtStartA", "idB",
				"titleB", "dtStartB" };
		int[] to = new int[] { R.id.idA, R.id.titleA, R.id.dtStartA, R.id.idB,
				R.id.titleB, R.id.dtStartB };

		SimpleAdapter adapter = new SimpleAdapter(this, listEntries,
				R.layout.events_list_item, from, to);
		listViewA.setAdapter(adapter);

		listViewA.setOnItemClickListener(getEventItemClickListener(listEntries,
				calenderAId, calenderBId));
	}

	private void printCalendarsToLog() {
		Cursor cur = getContentResolver().query(Calendars.CONTENT_URI,
				CAL_PROJECTION, null, null, null);
		List<String[]> calendars = getColumns(cur, CAL_PROJECTION);
		Log.i(TAG, "Print calendars");
		print("calendars", calendars);
	}

	/*
	 * private List<Map<String, String>> setCalendarEvents(String calendarId,
	 * ListView eventsListView) { String[] selectionArgs = new String[] {
	 * calendarId }; Uri uri = Events.CONTENT_URI; String selection =
	 * Events.CALENDAR_ID + " = ? AND original_id is null AND deleted = 0";
	 * String order = Events.DTSTART; Cursor cur =
	 * getContentResolver().query(uri, EVENT_PROJECTION, selection,
	 * selectionArgs, order); List<String[]> calEvents = getColumns(cur,
	 * EVENT_PROJECTION); print(calendarId + ": ", calEvents); List<Map<String,
	 * String>> values = getValues(calEvents); String[] from = new String[] {
	 * "title", "dtstart" }; int[] to = new int[] { R.id.item1, R.id.item2 };
	 * 
	 * SimpleAdapter adapter = new SimpleAdapter(this, values,
	 * R.layout.list_item, from, to); eventsListView.setAdapter(adapter); return
	 * values; }
	 */
	private void setCalendarName(String calendarId, TextView calTextView) {
		String selection = Calendars._ID + " = ?";
		String[] selectionArgs = new String[] { calendarId };

		Cursor cur = getContentResolver().query(Calendars.CONTENT_URI,
				CAL_PROJECTION, selection, selectionArgs, null);
		cur.moveToFirst();

		calTextView.setText(cur.getString(CAL_PROJECTION_NAME) + " "
				+ cur.getString(CAL_PROJECTION_LOCATION));
	}

	private OnItemClickListener getEventItemClickListener(
			final List<Map<String, String>> values, final String calendarId,
			final String targetCalendarId) {
		return new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(MainActivity.this,
						EventActivity.class);
				intent.putExtra("idA",values.get(position).get("idA"));
				intent.putExtra("idB",values.get(position).get("idB"));
				intent.putExtra("calenderIdA", calendarId);
				intent.putExtra("calenderIdB", targetCalendarId);
				startActivity(intent);
			}
		};
	}

	private List<Map<String, String>> getValues(List<String[]> events) {
		List<Map<String, String>> values = new ArrayList<Map<String, String>>();

		for (String[] event : events) {
			Map<String, String> map1 = new HashMap<String, String>();
			map1.put("id", event[PROJECTION_ID_INDEX]);
			map1.put("title", event[PROJECTION_TITLE]);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(Long.parseLong(event[PROJECTION_DTSTART]));
			DateFormat format = SimpleDateFormat.getDateTimeInstance();
			map1.put("dtstart", format.format(cal.getTime()));
			values.add(map1);
		}
		return values;
	}

	private List<String[]> getColumns(Cursor cur, String[] projection) {
		List<String[]> columns = new ArrayList<String[]>();
		while (cur.moveToNext()) {
			String[] column = new String[projection.length];
			for (int i = 0; i < projection.length; i++) {
				column[i] = cur.getString(i);
			}
			columns.add(column);
		}
		return columns;
	}

	private void print(String prefix, List<String[]> columns) {
		for (String[] column : columns) {
			String out = "";
			for (int i = 0; i < column.length; i++) {
				out += column[i] + ", ";
			}
			Log.i(TAG, prefix + ": " + out);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
