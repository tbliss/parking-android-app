/**
 * Copyright 2013 Trevor Bliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tbliss.parkingapp;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener, LocationListener {
	private static final String LOG_TAG = "MainActivity";

	// GPS
	private LocationManager mLocManager;
	private Location mCurrLoc;
	private long MIN_TIME = 500;
	private float MIN_LOC = 0;

	// Current user selection
	private long mCurrTime = 0;
	private long mCurrDay = 0;

	// Saved data
	private double mSavedLat;
	private double mSavedLon;

	// Layouts
	private Button mSaveButton;
	private EditText mEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSaveButton = (Button) findViewById(R.id.save_button);
        mEditText = (EditText) findViewById(R.id.note_editText);

        initSpinners();

        mLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save data
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putLong("time", mCurrTime);
        editor.putLong("day", mCurrDay);
        editor.putString("lat", "" + mSavedLat);
        editor.putString("lon", "" + mSavedLon);
        editor.putString("notes", mEditText.getText().toString());
        editor.commit();

        mLocManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_LOC, this);
        mSaveButton.setEnabled(false); // wait for first location update
        mSaveButton.setText(R.string.waiting_for_location);

        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        Long time = sharedPrefs.getLong("time", 0);
        long day = sharedPrefs.getLong("day", 0);
        setTime(time);
        setDay(day);

        String lat = sharedPrefs.getString("lat", "0.0");
        String lon = sharedPrefs.getString("lon", "0.0");
        mSavedLat = Double.parseDouble(lat);
        mSavedLon = Double.parseDouble(lon);
        
        String notes = sharedPrefs.getString("notes", "");
        mEditText.setText(notes);
    }

	private void setTime(long time) {
	    mCurrTime = time;
    	Spinner spinner = (Spinner) findViewById(R.id.time_spinner);   	
    	spinner.setSelection((int)time);
    }
	
	private void setDay(long day) {
	    mCurrDay = day;
	    Spinner spinner = (Spinner) findViewById(R.id.day_spinner);
	    spinner.setSelection((int)day);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    private void initSpinners() {
        // Time of day
    	Spinner timeSpinner = (Spinner) findViewById(R.id.time_spinner);
    	ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
    	        R.array.time_array, android.R.layout.simple_spinner_item);
    	timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	timeSpinner.setAdapter(timeAdapter);
    	timeSpinner.setOnItemSelectedListener(this);
    	
    	// Day of week
    	Spinner daySpinner = (Spinner) findViewById(R.id.day_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.day_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
        daySpinner.setOnItemSelectedListener(this);
    }

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    switch(parent.getId()) {
	    case R.id.time_spinner:
	        Log.i(LOG_TAG, "time: " + id);
	        mCurrTime = id;
	        break;
	    case R.id.day_spinner:
	        mCurrDay = id;
	        break;
	    }
	}

	public void onNothingSelected(AdapterView<?> arg0) {}

	/**
	 * Callback for Save parking button
	 */
	public void saveParking(View view) {
		Log.v(LOG_TAG, "saveParking() time: " + mCurrTime + ", day: " + mCurrDay);
		Log.v(LOG_TAG, "saveParking() lat: " + mCurrLoc.getLatitude() + ", lon: " + mCurrLoc.getLongitude());

		mSavedLat = mCurrLoc.getLatitude();
		mSavedLon = mCurrLoc.getLongitude();

		Toast.makeText(getApplicationContext(), "Parking location saved", Toast.LENGTH_SHORT).show();
	}

	/**
	 * Callback for Get Parking Spot button.
	 */
	public void getParkingLocation(View view) {
		Log.v(LOG_TAG, "getParkingLocation(): " + mSavedLat + ", " + mSavedLon);
		String uri = String.format("http://maps.google.com/maps?saddr=37.777964,-122.441783&daddr=%s,%s", ""
		            + mSavedLat, "" + mSavedLon);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
		startActivity(intent);
	}
	
	public void clearEditText(View view) {
	    mEditText.setText("");
	}

	/**
	 * Callback for Set Calendar Reminder button.
	 */    
	public void setCalendarReminder(View view) {
	    Intent calIntent = new Intent(Intent.ACTION_INSERT);
	    calIntent.setType("vnd.android.cursor.item/event");
	    calIntent.putExtra(Events.TITLE, "Move car for street cleaning");
	    calIntent.putExtra(Events.DESCRIPTION, mEditText.getText().toString());
	    calIntent.putExtra("beginTime", getParkingTime());
	    startActivity(calIntent);
	}

	private long getParkingTime() {
	    Calendar cal = Calendar.getInstance();

        int currHour = cal.get(Calendar.HOUR_OF_DAY);
        int currDay = cal.get(Calendar.DAY_OF_WEEK);
        int parkingDay = (int)mCurrDay + 1;
        int parkingHour = (int)mCurrTime + 6;
        int incrementDay = 0;
        if (currDay == parkingDay) {
            if (parkingHour < currHour) {
                incrementDay = 7;
            }
        } else if (parkingDay > currDay) {
            incrementDay = parkingDay - currDay;
        } else {
            incrementDay = (parkingDay + 7) - currDay;
        }
        Log.i(LOG_TAG, "parkingDay: " + parkingDay);
        Log.i(LOG_TAG, "currDay: " + currDay);
        Log.i(LOG_TAG, "incrementDay: " + incrementDay);
        cal.roll(Calendar.DAY_OF_WEEK, incrementDay);
        cal.set(Calendar.HOUR_OF_DAY, parkingHour);
        cal.set(Calendar.MINUTE, 0);

        return cal.getTimeInMillis();
        
	}

	public void onLocationChanged(Location location) {
		// Log.v(LOG_TAG, "locationChange");
		mCurrLoc = location;
        mSaveButton.setEnabled(true);
        mSaveButton.setText(R.string.save_button);
	}

	public void onProviderDisabled(String provider) {}

	public void onProviderEnabled(String provider) {}

	public void onStatusChanged(String provider, int status, Bundle extras) {}
}

// TODO:
// check if gps enabled, what to do if turned off

