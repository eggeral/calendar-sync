package com.axegr.calendarsync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

	public static final String[] CAL_PROJECTION = new String[] { Calendars._ID,
			Calendars.NAME };

	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] { Events._ID, // 0
			Events.TITLE, // 1
			Events._SYNC_ID, Events.SYNC_DATA1, // 2
			Events.SYNC_DATA2 // 3
	};

	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_DESCRIPTION = 1;
	private static final int PROJECTION_CALENDAR_DISPLAY_NAME = 2;
	private static final int PROJECTION_DTSTART = 3;

	private static final String TAG = "calsync";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Cursor cur = null;
		ContentResolver cr = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
		cur = cr.query(uri, CAL_PROJECTION, null, null, null);
		List<String[]> calendars = getColumns(cur, CAL_PROJECTION);
		Log.i(TAG, "Print calendars");
		print("calendars", calendars);

		uri = Events.CONTENT_URI;
		String selection = Events.CALENDAR_ID + " = ? ORDER BY " +Events.DTSTART;
		String[] selectionArgs = new String[] { "1" }; // 11
		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		List<String[]> cal1Events = getColumns(cur, EVENT_PROJECTION);
		print("cal 1 event", cal1Events);

		selectionArgs = new String[] { "11" };
		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		List<String[]> cal11Events = getColumns(cur, EVENT_PROJECTION);
		print("cal 11 event", cal11Events);

		ListView listView = (ListView) findViewById(R.id.eventList);

		List<Map<String, String>> values = new ArrayList<Map<String, String>>();
		for (String[] event : cal11Events) {
			Map<String, String> map1 = new HashMap<String, String>();
			map1.put("col1", event[0]);
			map1.put("title", event[1]);
			values.add(map1);			
		}
		
		String[] from = new String[] { "col1", "title" };
		int[] to = new int[] { R.id.item1, R.id.item2 };

		SimpleAdapter adapter = new SimpleAdapter(this, values,
				R.layout.list_item, from, to);

		// Assign adapter to ListView
		listView.setAdapter(adapter);

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
