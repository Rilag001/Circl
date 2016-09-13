package se.rickylagerkvist.circl;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class SettingsActivity2 extends AppCompatActivity {

    private SeekBar mSearchAreaSeekBar;
    private TextView mSearchAreaTextView, mSelectStartTimeTextView, mSelectStopTimeTextView;
    private int mSearchAreaSeekBarProgress;
    int searchAreaNr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init views
        mSearchAreaSeekBar = (SeekBar) findViewById(R.id.seekBarSearchArea);
        mSearchAreaTextView = (TextView) findViewById(R.id.searchAreaTextView);
        mSelectStartTimeTextView = (TextView) findViewById(R.id.startServiceText);
        mSelectStopTimeTextView = (TextView) findViewById(R.id.stopServiceText);


        // AreaSeekBar
        mSearchAreaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (seekBar.getProgress() == 0){
                    mSearchAreaTextView.setText(getString(R.string.search_area) + ": 100 m");
                } else if (seekBar.getProgress() > 0 && seekBar.getProgress() <= 9) {
                    int i = mSearchAreaSeekBar.getProgress() + 1;
                    mSearchAreaTextView.setText(getString(R.string.search_area) + ": " + i + "00 m");
                } else {
                    int i = mSearchAreaSeekBar.getProgress() + 1;
                    String s = Integer.toString(i);
                    s = s.substring(0, 1) + "." + s.substring(1, s.length());
                    mSearchAreaTextView.setText(getString(R.string.search_area) + ": " + s + " km");
                }
            }
        });

        // select startTime
        mSelectStartTimeTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(SettingsActivity2.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        String hour;
                        if(selectedHour < 10){
                            hour = "0"+ selectedHour;
                        } else {
                            hour = String.valueOf(selectedHour);
                        }

                        String min;
                        if(selectedMinute < 10){
                            min = "0"+ selectedMinute;
                        } else {
                            min = String.valueOf(selectedMinute);
                        }

                        mSelectStartTimeTextView.setText( "Start: " + hour + ":" + min);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        // select startTime
        mSelectStopTimeTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(SettingsActivity2.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        String hour;
                        if(selectedHour < 10){
                            hour = "0"+ selectedHour;
                        } else {
                            hour = String.valueOf(selectedHour);
                        }

                        String min;
                        if(selectedMinute < 10){
                            min = "0"+ selectedMinute;
                        } else {
                            min = String.valueOf(selectedMinute);
                        }


                        mSelectStopTimeTextView.setText( "Stop: " + hour + ":" + min);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        // done, finish activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                Snackbar.make(view, "Settings Saved", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                // stop in 10 sec
                new CountDownTimer(2000, 1000) { // 5000 = 5 sec

                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        finish();
                    }
                }.start();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // save mSearchAreaSeekBar Progress and text to preference
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("SEARCH_AREA", mSearchAreaTextView.getText().toString()).apply();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("SEARCH_AREA_NR", mSearchAreaSeekBar.getProgress()).apply();

        // save mSelectStartTimeTextView and mSelectStopTimeTextView
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("START_TIME", mSelectStartTimeTextView.getText().toString()).apply();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("STOP_TIME", mSelectStopTimeTextView.getText().toString()).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // load mSearchAreaSeekBar Progress and text from preference
        String searchArea = PreferenceManager.getDefaultSharedPreferences(this).getString("SEARCH_AREA", "0");
        searchAreaNr = PreferenceManager.getDefaultSharedPreferences(this).getInt("SEARCH_AREA_NR", 0);
        mSearchAreaSeekBarProgress = PreferenceManager.getDefaultSharedPreferences(this).getInt("SEARCH_AREA_NR", 0);
        // set mSearchAreaSeekBar.setProgress(position);
        mSearchAreaTextView.setText(searchArea);
        mSearchAreaSeekBar.setProgress(searchAreaNr);

        // load save mSelectStartTimeTextView and mSelectStopTimeTextView
        String start = PreferenceManager.getDefaultSharedPreferences(this).getString("START_TIME", "0");
        String stop = PreferenceManager.getDefaultSharedPreferences(this).getString("STOP_TIME", "0");
        // set mSelectStartTimeTextView and mSelectStopTimeTextView
        mSelectStartTimeTextView.setText(start);
        mSelectStopTimeTextView.setText(stop);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
