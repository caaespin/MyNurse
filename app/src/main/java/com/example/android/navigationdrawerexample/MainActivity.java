/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.navigationdrawerexample;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
//import android.support.v4.app.Fragment;
import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
//import android.support.v4.app.DialogFragment;
import android.app.DialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.PopupWindow;

/**
 * Based on the android DrawerLayout example available on the online resource
 * This app allows the user to schedule Dr. Appointments, as well as schedule a drug regime.
 * The schedulings are set using an intent leading to the Google Calendar app. I use a recurssion
 * rule to schedule multiple events throught the day and for multiple days.
 */

public class MainActivity extends Activity {
    //For the popUp window
    static Button btnClosePopup;
    static Button btnCreatePopup;
    static View roottie;
    //----------------------------------------------------------------------------------------------
    //For the drug scheduling interface
    static TextView myDrugSummary;
    //----------------------------------------------------------------------------------------------

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;

    //location---------
    public static String LOG_TAG = "MyMapApplication";
    private LocationData locationData = LocationData.getLocationData();
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    double latitude = 0.0;
    double longitude = 0.0;
    boolean hasLocation = false;
    //---------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //---------Location---------------------------------------
        locationData.getLocation();
        requestLocationUpdate();
       //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences.Editor e = settings.edit();
        if(locationData.getLocation() != null){
            latitude= locationData.getLocation().getLatitude();
            longitude =  locationData.getLocation().getLongitude();

        }
        //--------------------------------------------------------

        mTitle = mDrawerTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(R.array.state_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }


    //I guess i can use this so seniors can check anything they want on the internet?
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {

        case R.id.action_websearch:
            // create intent to perform web search for this planet
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, "");//getActionBar().getTitle());
            // catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }




    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
            //if(position==1) dotheListen();
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putInt(ContentFragment.ARG_ACTIVITY_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        //Testing for the popup thingy
        //if(position==1) dotheListen();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public  class ContentFragment extends Fragment {
        public static final String ARG_ACTIVITY_NUMBER = "activity_number";

        public ContentFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_ACTIVITY_NUMBER);
            flagTime = false;
            flagDate = false;
            View rootView;
            switch (i) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_activitydrugs, container, false);
                    roottie = rootView;
                    btnCreatePopup = (Button) rootView.findViewById(R.id.createPop);
                    myDrugSummary = (TextView) rootView.findViewById(R.id.drugSummary);
                    dotheListen();
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_appointments, container, false);
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_pharmacy, container, false);
                    requestLocationUpdate();
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_summary, container, false);
                    break;
            }
            String whichActivity = getResources().getStringArray(R.array.state_array)[i];
            getActivity().setTitle(whichActivity);
            return rootView;
        }
    }

    /**
     * The following code is for the case 1: drugs view. It handles the scheduling of
     * drugs for the patient.
     * It should schedule the dosage, frequency, and length of a drug regime.
     * It may use Google Event Calendar? Using google calendar would ensure it's on the cloud, and sinc
     * with other devices or computer the user might use.
     * RRULE:FREQ=DAILY; BYHOUR= 9, 15, 20
     * **/
    List<Float> drugsID = new ArrayList<>();
    public void submitDrug(View v) {
        //Will check if these fields have been filled.
        RadioGroup g = (RadioGroup) findViewById(R.id.methodOfDelivery);
        int selected = g.getCheckedRadioButtonId();
        String methodOf = ((RadioButton) findViewById(selected)).getText().toString();
        String nOfD = ((EditText) findViewById(R.id.drugname)).getText().toString();
        String dsg = ((EditText) findViewById(R.id.dosage)).getText().toString();
        String numOfDays = ((EditText) findViewById(R.id.numofdays)).getText().toString();
        //Integer numOfDaysNum = Integer.parseInt(((EditText) findViewById(R.id.numofdays)).getText().toString());

        boolean youHaveAmPm = !(selectedHoursAm.isEmpty() && selectedHoursPm.isEmpty());

        boolean test = (selected!=-1 && !nOfD.isEmpty() && !dsg.isEmpty() && !numOfDays.isEmpty() && youHaveAmPm );
        test = test && !intHoursSel.isEmpty();
        if(test){
            //EditText mEdit   = (EditText)findViewById(R.id.theAddress);
            String theCount = String.valueOf(Integer.parseInt(numOfDays) * (selectedHoursAm.size() + selectedHoursPm.size()));
            String hoursIn = "";
            for(int i=0; i<intHoursSel.size(); i++){
                if(i==(intHoursSel.size()-1)) {
                    hoursIn = hoursIn+intHoursSel.get(i).toString();
                    break;
                }
                hoursIn = hoursIn+intHoursSel.get(i).toString()+",";
            }

            //-------------For some reason it seems that the recursion works when done HOURLY only------------
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");

            intent.putExtra(Events.TITLE, "Drug Reminder");
            intent.putExtra(Events.DESCRIPTION, "You have to take " + nOfD+" " + dsg +"; "+methodOf);
            intent.putExtra(Events.RRULE, "FREQ=HOURLY;BYHOUR=" + hoursIn + ";COUNT=" + theCount);//;BYHOUR=21,22,23;COUNT="+theCount);//10;BYHOUR=21,22,23");//;BYHOUR=9, 15, 20");
            startActivity(intent);
            //---------------------------------------------------



            //Clear the existing fields on the app
            ((EditText) findViewById(R.id.drugname)).setText("");
            ((EditText) findViewById(R.id.drugname)).clearFocus();
            ((EditText) findViewById(R.id.dosage)).setText("");
            ((EditText) findViewById(R.id.dosage)).clearFocus();
            ((EditText) findViewById(R.id.numofdays)).setText("");
            ((EditText) findViewById(R.id.numofdays)).clearFocus();
            g.clearFocus();
            selectedHoursAm.clear();
            selectedHoursPm.clear();
            intHoursSel.clear();
            ((TextView) findViewById(R.id.drugSummary)).setText("Current Hours:");
        }
        else{
            Context context = getApplicationContext();
            CharSequence text = "Please fill out all the fields";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
    }


    /**
     * The following code is for the case 2: appointments view. It handles all the
     * instructions associated with it.
     * It can schedule appointments with the date, time, and address.
     * It uses Google's Event Calendar.
     * **/
    static boolean flagTime = false;
    static boolean flagDate = false;
    static Appointment myAppointment = new Appointment();
    static TextView bernie;
    //static View cheese;

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            flagTime = true;
            myAppointment.hour = hourOfDay;
            myAppointment.minute = minute;
            bernie.setText("at "+ myAppointment.hour +":"+myAppointment.minute);
            bernie.setVisibility(View.VISIBLE);
            // Do something with the time chosen by the user
        }
    }

    //Used for showing the time.
    public void showTimePickerDialog(View v) {
        TextView theTime = (TextView) findViewById(R.id.tosumup);
        bernie = theTime;

        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            flagDate = true;
            myAppointment.year = year;
            myAppointment.month = month+1;
            myAppointment.day = day;

            bernie.setText("Your appointment is on " + myAppointment.day + "/" + myAppointment.month + "/"+myAppointment.year);
            bernie.setVisibility(View.VISIBLE);
            // Do something with the date chosen by the user
        }
    }
    //Used for showing the date.
    public void showDatePickerDialog(View v) {
        TextView theTime = (TextView) findViewById(R.id.textView2);
        bernie = theTime;

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    //This button will submit the appointment to the calendar
    public void submitAppointment(View v) {
        if(flagDate && flagTime){
            EditText mEdit   = (EditText)findViewById(R.id.theAddress);
            String theAddress = mEdit.getText().toString();
            if(theAddress == null || theAddress.equals("")){
                Context context = getApplicationContext();
                CharSequence text = "Please Insert an Address";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(myAppointment.year, myAppointment.month, myAppointment.day, myAppointment.hour, myAppointment.minute);
            end.set(myAppointment.year, myAppointment.month, myAppointment.day, myAppointment.hour + 1, myAppointment.minute);
            //Time theTime = new Time(myAppointment.hour, myAppointment.minute, 0);
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin.getTimeInMillis());
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.getTimeInMillis());
            intent.putExtra(Events.TITLE, "Appointment");
            //intent.putExtra(Events.DESCRIPTION, "This is a sample description");
            intent.putExtra(Events.EVENT_LOCATION, theAddress);
            startActivity(intent);

            mEdit.setText("");
            mEdit.clearFocus();
            flagTime = false;
            flagDate = false;
            TextView theTime = (TextView) findViewById(R.id.tosumup);
            TextView theTime2 = (TextView) findViewById(R.id.textView2);
            theTime.setVisibility(View.INVISIBLE);
            theTime2.setVisibility(View.INVISIBLE);

            //Appointment myAppointment = new Appointment();
            //myAppointment.year =  get
        }
        else{
            Context context = getApplicationContext();
            CharSequence text = "Please Set the Date and Time";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
    }


    /**
     * The following code is for the case 3: Pharmacy. It should show a map with the nearest pharmacies
     * **/

    public void pharmacy(View v){
        String url = "http://maps.google.co.uk/maps?q=Pharmacy&hl=en";
        final Intent map = new Intent(this, MapsActivity.class);

        final ProgressDialog dialog = new ProgressDialog(this); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading location. Please wait...");
        dialog.setIndeterminate(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                dialog.dismiss();
                startActivity(map);
            }
        }, 3000);

        //startActivity(map);

    }





    /**
     * Code for dealing with the popup window. Based on code from the tutorial and website
     * http://www.androidhub4you.com/2012/07/how-to-create-popup-window-in-android.html
     * **/

    private void dotheListen(){

        //btnCreatePopup = (Button) findViewById(R.id.createPop);
        btnCreatePopup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                initiatePopupWindow();
            }
        });
        return;
    }

    private PopupWindow pwindo;
    View popUp;

    private void initiatePopupWindow() {
        try {
    // Get the instance of the LayoutInflater
            selectedHoursAm.clear();
            selectedHoursPm.clear();
            LayoutInflater inflater = (LayoutInflater) MainActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_window,
                    (ViewGroup) findViewById(R.id.popup_element));
            popUp = layout;
            pwindo = new PopupWindow(layout, 1000, 1300, true);
            pwindo.setAnimationStyle(R.style.animationName);//checking the animation_on style
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

            btnClosePopup = (Button) layout.findViewById(R.id.btn_close_popup);
            btnClosePopup.setOnClickListener(cancel_button_click_listener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private View.OnClickListener cancel_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            //popUp.setVisibility();
            Collections.sort(selectedHoursAm);
            Collections.sort(selectedHoursPm);
            Collections.sort(intHoursSel);
            myDrugSummary.setText("Current Hours:");
            for(int i = 0; i< selectedHoursAm.size();i++) {
                myDrugSummary.append(" " + selectedHoursAm.get(i));
            }
            for(int i = 0; i< selectedHoursPm.size();i++) {
                myDrugSummary.append(" " + selectedHoursPm.get(i));
            }

            pwindo.dismiss();
            //See if i can set a text on the original view?


        }
    };
    List<Integer> intHoursSel = new ArrayList<>();
    List<String> selectedHoursAm = new ArrayList<>();
    List<String> selectedHoursPm = new ArrayList<>();

    public void checkedBox(View v){
        CheckBox choice = (CheckBox) v.findViewById(((CheckBox) v).getId());//returns the assigned ID long value
        //Found out that this runs after the checkbox gets checked. Or at least that's what it seems
        Integer numHourIs = Integer.parseInt(choice.getTag().toString());
        String hour = choice.getText().toString();
        String amOrPm = String.valueOf(hour.charAt(2));
        if(choice.isChecked()) {
            if(amOrPm.equals("p")) {
                selectedHoursPm.add(hour);
                intHoursSel.add(numHourIs);
            }
            else {
                selectedHoursAm.add(hour);
                intHoursSel.add(numHourIs);
            }

        }
        else{
            //String hour = choice.getText().toString();
            if(amOrPm.equals("p")){
                selectedHoursPm.remove(hour);
                intHoursSel.remove(numHourIs);
            }
            else {
                selectedHoursAm.remove(hour);
                intHoursSel.remove(numHourIs);
            }
        }

        //read anything you want - check the developers for more details
    }


    //---------------------------Code for Location -------------------------------------------------
    private void requestLocationUpdate() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 35000, 10, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 35000, 10, locationListener);

                hasLocation = true;

                Log.i(LOG_TAG, "requesting location update");
            } else {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.i(LOG_TAG, "please allow to use your location");

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        } else {
            Log.i(LOG_TAG, "requesting location update from user");
            //prompt user to enable location
            Intent gpsOptionsIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            Location lastLocation = locationData.getLocation();

            // Do something with the location you receive.
            double newAccuracy = location.getAccuracy();

            long newTime = location.getTime();
            // Is this better than what we had?  We allow a bit of degradation in time.
            boolean isBetter = ((lastLocation == null) ||
                    newAccuracy < lastLocation.getAccuracy() + (newTime - lastLocation.getTime()));
            if (isBetter) {
                // We replace the old estimate by this one.
                locationData.setLocation(location);
            }
            hasLocation = true;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };
}