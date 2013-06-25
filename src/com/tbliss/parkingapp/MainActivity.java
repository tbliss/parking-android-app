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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class MainActivity extends Activity implements OnItemSelectedListener, LocationListener {
	private static final String LOG_TAG = "MainActivity";

	// GPS
	private LocationManager mLocManager;
	private Location mCurrLoc;
	private long MIN_TIME = 500;
	private float MIN_LOC = 0;

	// Current user selection
	private String mCurrTime = "8:00";
	private DAY mCurrDay = DAY.NONE;

	// Saved data
	private double mSavedLat;
	private double mSavedLon;

	// Layouts
	private Button mSaveButton;

	private enum DAY {
		NONE, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set None as default street cleaning day
        RadioButton noneDay = (RadioButton) findViewById(R.id.radio_none);
        noneDay.setChecked(true);

        mSaveButton = (Button) findViewById(R.id.save_button);

        initSpinner();

        mLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save data
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString("time", mCurrTime);
        editor.putString("day", mCurrDay.toString());
        editor.putString("lat", "" + mSavedLat);
        editor.putString("lon", "" + mSavedLon);
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
        String time = sharedPrefs.getString("time", "8:00");
        String day = sharedPrefs.getString("day", "NONE");
        setTime(time);
        setDay(day);
        String lat = sharedPrefs.getString("lat", "0.0");
        String lon = sharedPrefs.getString("lon", "0.0");
        mSavedLat = Double.parseDouble(lat);
        mSavedLon = Double.parseDouble(lon);

        Log.i(LOG_TAG, "onResume() lat: " + mSavedLat + ", lon: " + mSavedLon);
    }

    private void setDay(String day) {
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.days_group);
        if (day.equals("MONDAY")) {
            mCurrDay = DAY.MONDAY;
            radioGroup.check(R.id.radio_monday);
        } else if (day.equals("TUESDAY")) {
            mCurrDay = DAY.TUESDAY;
            radioGroup.check(R.id.radio_tuesday);
        } else if (day.equals("WEDNESDAY")) {
            mCurrDay = DAY.WEDNESDAY;
            radioGroup.check(R.id.radio_wednesday);
        } else if (day.equals("THURSDAY")) {
            mCurrDay = DAY.THURSDAY;
            radioGroup.check(R.id.radio_thursday);
        } else if (day.equals("FRIDAY")) {
            mCurrDay = DAY.FRIDAY;
            radioGroup.check(R.id.radio_friday);
        } else {
            mCurrDay = DAY.NONE;
            radioGroup.check(R.id.radio_none);
        }
    }

	@SuppressWarnings("unchecked") // ArrayAdapter
    private void setTime(String time) {
	    mCurrTime = time;
    	Spinner spinner = (Spinner) findViewById(R.id.time_spinner);   	
    	ArrayAdapter<String> arrayAdap = (ArrayAdapter<String>) spinner.getAdapter();
    	int position = arrayAdap.getPosition(time);
    	spinner.setSelection(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void initSpinner() {
    	Spinner spinner = (Spinner) findViewById(R.id.time_spinner);
    	// Create an ArrayAdapter using the string array and a default spinner layout
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
    	        R.array.time_array, android.R.layout.simple_spinner_item);
    	// Specify the layout to use when the list of choices appears
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinner.setAdapter(adapter);
    	spinner.setOnItemSelectedListener(this);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
        	case R.id.radio_none:
        		if (checked)
        			mCurrDay = DAY.NONE;
        		break;
            case R.id.radio_monday:
                if (checked)
                	mCurrDay = DAY.MONDAY;
                break;
            case R.id.radio_tuesday:
                if (checked)
                	mCurrDay = DAY.TUESDAY;
                break;
            case R.id.radio_wednesday:
                if (checked)
                	mCurrDay = DAY.WEDNESDAY;
                break;
            case R.id.radio_thursday:
                if (checked)
                	mCurrDay = DAY.THURSDAY;
                break;
            case R.id.radio_friday:
                if (checked)
                	mCurrDay = DAY.FRIDAY;
                break;
            default:
            	// Should never be here...
            	break;
        }
    }

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    mCurrTime = (String) parent.getItemAtPosition(pos);
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
	}

	/**
	 * Callback for Get Parking Spot button.
	 */
	public void getParkingLocation(View view) {
		Log.v(LOG_TAG, "getParkingLocation(): " + mSavedLat + ", " + mSavedLon);
		String uri = String.format("http://maps.google.com/maps?saddr=37.777964,-122.441783&daddr=%s,%s&mode=walking", 
				"" + mSavedLat, "" + mSavedLon);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
		startActivity(intent);
	}

	/**
	 * Callback for Set Calendar Reminder button.
	 */
	public void setCalendarReminder(View view) {
	    Intent calIntent = new Intent(Intent.ACTION_INSERT);
	    calIntent.setType("vnd.android.cursor.item/event");
	    calIntent.putExtra(Events.TITLE, "Move car for street cleaning");
	    startActivity(calIntent);
	}
	
	private Calendar parkingTime() {
	    Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        Log.i(LOG_TAG, "year: " + year + ", month: " + month + ", day: " + day);
        return cal;
        
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
// change spinner to picker
// check if gps enabled, what to do if turned off

