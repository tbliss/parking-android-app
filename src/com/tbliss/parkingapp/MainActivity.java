package com.tbliss.parkingapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

public class MainActivity extends Activity implements OnItemSelectedListener {
	private static final String LOG_TAG = "MainActivity";
	
	private String TIME = "8:00";
	private DAY Day = DAY.NONE;
	
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
        
        initSpinner();
        Spinner spinner = (Spinner) findViewById(R.id.time_spinner);
        spinner.setOnItemSelectedListener(this);
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
    }
    
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        
        switch(view.getId()) {
        	case R.id.radio_none:
        		if (checked)
        			Day = DAY.NONE;
        		break;
            case R.id.radio_monday:
                if (checked)
                	Day = DAY.MONDAY;
                break;
            case R.id.radio_tuesday:
                if (checked)
                	Day = DAY.TUESDAY;
                break;
            case R.id.radio_wednesday:
                if (checked)
                	Day = DAY.WEDNESDAY;
                break;
            case R.id.radio_thursday:
                if (checked)
                	Day = DAY.THURSDAY;
                break;
            case R.id.radio_friday:
                if (checked)
                	Day = DAY.FRIDAY;
                break;
            default:
            	// Should never be here...
            	break;
        }
    }

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		TIME = (String) parent.getItemAtPosition(pos);
		Log.v(LOG_TAG, TIME);
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		Log.v(LOG_TAG, "meep2");
	}
}

// TODO:
// change spinner to picker

