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
import android.content.ContentValues;
import android.database.Cursor;
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
		long eventId = getIntent().getLongExtra("event_id", -1);
		final String calendarId = getIntent().getStringExtra("calendar_id");
		final String targetCalendarId = getIntent().getStringExtra("target_calendar_id");
		

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
		setContentView(R.layout.activity_event);

		ListView eventA = (ListView) findViewById(R.id.event_a);

		String[] from = new String[] { "name", "value" };
		int[] to = new int[] { R.id.event_item1, R.id.event_item2 };

		SimpleAdapter adapterA = new SimpleAdapter(this, valuesA,
				R.layout.event_properties_item, from, to);

		eventA.setAdapter(adapterA);
		
		Button addButton = (Button) findViewById(R.id.add_button);
		addButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i(TAG, "CLICK " + calendarId + ", target: " + targetCalendarId);
				ContentResolver cr = getContentResolver();
				ContentValues values = new ContentValues();
				copyColumn(cur, values, Events.DTSTART);
				copyColumn(cur, values, Events.DTEND);
				copyColumn(cur, values, Events.DURATION);
				copyColumn(cur, values, Events.TITLE);
				copyColumn(cur, values, Events.DESCRIPTION);
				copyColumn(cur, values, Events.EVENT_TIMEZONE);
				copyColumn(cur, values, Events.RDATE);
				copyColumn(cur, values, Events.RRULE);

				
				values.put(Events.CALENDAR_ID, targetCalendarId);
				Uri uri = cr.insert(Events.CONTENT_URI, values);
				
			}

			private void copyColumn(final Cursor cur, ContentValues values,
					String column) {
				values.put(column, cur.getString(cur.getColumnIndex(column)));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_event, menu);
		return true;
	}

}
