package edu.sjsu.jaime.fitnessapp;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static edu.sjsu.jaime.fitnessapp.BlankFragment.average;
import static edu.sjsu.jaime.fitnessapp.BlankFragment.maximum;
import static edu.sjsu.jaime.fitnessapp.BlankFragment.minimum;
import static edu.sjsu.jaime.fitnessapp.Utils.calculateMinutes;


/**
 * A simple {@link Fragment} subclass.
 */
public class portrait extends Fragment implements OnMapReadyCallback, SensorEventListener{

    private ArrayList<caloriesData> calData;
    private ArrayList<stepsData> stepData;
    private double caloriesChart = 0;

    private ArrayList<Integer> min = new ArrayList<>();
    private ArrayList<Integer> max = new ArrayList<>();

    //*****************EXERCISE NUMBERS********************************
    private static final double w_dist = 0.00067056;
    private static final double m_dist = 0.000762;
    private static final double[] calories = new double[]{0.025, 0.03, 0.04, 0.045, 0.05, 0.055, 0.062, 0.068, 0.075};
    private int caloriesUsed = 0;
    private double distanceUsed;
    private double dRounded = 0;

    //*************************SHARED PREFERENCES******************************
    private static final String Initial_Count_Key = "FootStepInitialCount";
    private static final String gender_key = "Gender";
    private static final String weight_key = "weight";
    private static final String workouts_key = "workouts";
    private SharedPreferences prefs;


    private int looper = 0;
    private int kmlooper = 0;
    private int chartlooper = 0;
    private boolean updatesOn = false;
    boolean stepsRunning;

    private static final String TAG = "deb";

    //********************SENSOR OBJECTS
    SensorManager sensorManager;
    int startingSteps;
    int totalSteps =0;
    int chartSteps=0;

    //********************VIEW OBJECTS**********************
    Button startButton;
    Button stopButton;
    Button pauseButton;
    Button getLoc;
    TextView latitudeView, longitudeView;
    TextView stepsView;
    ImageView userButton;
    boolean clicked = false;

    //********************RUNNABLES*************************
    Runnable startTime;
    Runnable startPost;
    //********************MAP VIEWS*************************

    private MapView mapView;
    private GoogleMap gmap;
    private LatLng here;
    private Marker mark;
    private Polyline polyline;
    private boolean newPolyline=false;
    private ArrayList<LatLng> polylineLocations;
    //private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";



    //********************LOCATION AND BACGROUND SERVICE COMPONENTS***************
    private final int REQUEST_CODE = 1;
    private MyReceiver myReceiver;
    private LocationUpdatesService mService = null;
    private boolean mBound = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("deb", "in onServiceConnected, mBound set to true");
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("deb", "in onServiceDisconnected, mBound set to False");
            mService = null;
            mBound = false;
        }
    };

    //***************CHRONOMETER COMPONENTS*****************
    private Chronometer chronometer;
    private boolean running;
    private long pauseOffSet;

    //********************HANDLER****************************
    public Handler threadHandler = new Handler();

    public portrait() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        calData = new ArrayList<>();
        stepData = new ArrayList<>();
        myReceiver = new MyReceiver();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //View for fragment
        if(!prefs.contains(weight_key))
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(weight_key, 150);
            editor.apply();
        }

        if(!prefs.contains(gender_key))
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(gender_key, "M");
            editor.apply();
        }
        Log.d("deb", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_portrait, container, false);

        //Initialize View objects
        chronometer = view.findViewById(R.id.chronometer);
        startButton = view.findViewById(R.id.startButton);
        userButton = view.findViewById(R.id.imageView);
        stepsView = view.findViewById(R.id.textView3);

        //Sensor stuff
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);


        //Location stuff


        //MapView setup
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
        else {
                Log.d("deb", "permission not granted");
            }
        }

        //Set the Runnable
        setRunnables();

        //Listeners
        setButtonListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(new Intent(getActivity(), LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onResume() {
        mapView.onResume();
        Log.d("deb", "In onResume()");

        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(myReceiver, new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));

        /*
        if (updatesOn)
        {
            startLocationUpdates();
        }
        */

    }

    @Override
    public void onStop()
    {
        Log.d("deb", "In onSttop()");
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            Log.d("deb", "in onStop(), mBound set to False");
            getActivity().unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Log.d("deb", "In onDestroyView()");
    }

    @Override
    public void onDestroy() {
        Log.d("deb", "in onDestroy()");
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        Log.d("deb", "in onDetach");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(18);
        //gmap.addMarker(new MarkerOptions().position(here));
        //gmap.moveCamera(CameraUpdateFactory.newLatLng(here));
    }

    public void setRunnables()
    {
        startTime = new Runnable() {
            @Override
            public void run() {
                if (!running) {
                    looper = 0;
                    running = true;
                    threadHandler.post(startPost);


                    caloriesData temp2 = new caloriesData(looper, caloriesChart);
                    calData.add(temp2);
                    if (BlankFragment.chart != null) {
                        BlankFragment.setChartUp(calData);
                    }

                    stepsData temp3 = new stepsData(looper, chartSteps);
                    stepData.add(temp3);
                    if (BlankFragment.chart2 != null)
                    {
                        BlankFragment.setChart2Up(stepData);
                    }

                    chartSteps = totalSteps;


                } else {
                    //timer
                    looper++;
                    Log.d("deb", "Loop: " + String.valueOf(looper));

                    //Calculate distance
                    String gender = prefs.getString(gender_key, "M");
                    if (gender.equals("M")) {
                        Log.d("dist", "It's a man");
                        double d = totalSteps * m_dist;
                        dRounded = Math.round(d * 100.0) / 100.0;
                        stepsView.setText(String.valueOf(dRounded));
                    } else {
                        Log.d("dist", "It's a woman");
                        double d = totalSteps * w_dist;
                        dRounded = Math.round(d * 100.0) / 100.0;
                        stepsView.setText(String.valueOf(dRounded));
                    }

                    //Calculate Calories
                    int weight = prefs.getInt(weight_key, 150);

                    if (weight < 120) {
                        caloriesUsed = (int) (totalSteps * calories[0]);
                        Log.d("cal", "Weight is below 120");
                        caloriesChart = ((totalSteps - chartSteps) * calories[0]);

                    } else if (weight < 140) {
                        caloriesUsed = (int) (totalSteps * calories[1]);
                        Log.d("cal", "Weight is below 140");
                        caloriesChart = ((totalSteps - chartSteps) * calories[1]);


                    } else if (weight < 160) {
                        caloriesUsed = (int) (totalSteps * calories[2]);
                        Log.d("cal", "Weight is below 160");
                        caloriesChart = ((totalSteps - chartSteps) * calories[2]);


                    } else if (weight < 180) {
                        caloriesUsed = (int) (totalSteps * calories[3]);
                        Log.d("cal", "Weight is below 180");
                        caloriesChart = ((totalSteps - chartSteps) * calories[3]);

                    } else if (weight < 200) {
                        caloriesUsed = (int) (totalSteps * calories[4]);
                        caloriesChart = ((totalSteps - chartSteps) * calories[4]);
                    } else if (weight < 220) {
                        caloriesUsed = (int) (totalSteps * calories[5]);
                        Log.d("cal", "Weight is below 220");
                        caloriesChart = ((totalSteps - chartSteps) * calories[5]);

                    } else if (weight < 250) {
                        caloriesUsed = (int) (totalSteps * calories[6]);
                        Log.d("cal", "Weight is below 250");
                        caloriesChart = ((totalSteps - chartSteps) * calories[6]);
                    } else if (weight < 275) {
                        caloriesUsed = (int) (totalSteps * calories[7]);
                        Log.d("cal", "Weight is below 275");
                        caloriesChart = ((totalSteps - chartSteps) * calories[7]);
                    } else {
                        caloriesUsed = (int) (totalSteps * calories[8]);
                        Log.d("cal", "Weight is above 275");
                        caloriesChart = ((totalSteps - chartSteps) * calories[8]);
                    }

                    if (User.time != null) {
                        User.time.setText(String.valueOf(User.timeTotal / 5 + looper));
                    }
                    if (User.allTime != null) {
                        User.allTime.setText(String.valueOf(User.timeTotal + looper));
                    }
                    if (User.distance != null) {
                        User.distance.setText(String.valueOf(User.weekdistRounded + dRounded));
                    }
                    if (User.allDistance != null) {
                        User.allDistance.setText(String.valueOf(User.distanceRounded + dRounded));
                    }
                    if (User.calories != null) {
                        User.calories.setText(String.valueOf(User.caloriesTotal / 5 + caloriesUsed));
                    }
                    if (User.allCalories != null) {
                        User.allCalories.setText(String.valueOf(User.caloriesTotal + caloriesUsed));
                    }

                    //make chart stuff
                    //Make views
                    if (BlankFragment.average != null) {
                        if (totalSteps !=0)
                            if (gender.equals("M"))
                                BlankFragment.average.setText(Utils.calculateMinutes((long) (looper / (totalSteps*m_dist))));
                            else
                                BlankFragment.average.setText(Utils.calculateMinutes((long) (looper / (totalSteps*w_dist))));

                    }

                    stepsData temp4 = new stepsData(looper, totalSteps);
                    stepData.add(temp4);

                    if (BlankFragment.chart2 != null)
                    {
                        BlankFragment.setChart2Up(stepData);
                    }
                    if (looper % 60 == 0) {
                        caloriesData temp2 = new caloriesData(looper, caloriesChart);
                        calData.add(temp2);
                        if (BlankFragment.chart != null) {
                            BlankFragment.setChartUp(calData);
                        }


                        if (gender.equals("M"))
                        {
                            if (totalSteps - chartSteps != 0)
                            min.add((int)(60/((totalSteps - chartSteps)*m_dist)));
                        }
                        else
                        {
                            if (totalSteps - chartSteps != 0)
                                min.add((int)(60/((totalSteps - chartSteps)*w_dist)));
                        }

                        if(BlankFragment.minimum != null)
                        {
                            int test = min.get(0);
                            for(int i : min)
                            {
                                if (i < test)
                                {
                                    test = i;
                                }

                            }

                            minimum.setText(String.valueOf(Utils.calculateMinutes((long)test)));
                        }

                        if (BlankFragment.maximum != null)
                        {
                            int test = min.get(0);
                            for(int i : min)
                            {
                                if (i > test)
                                {
                                    test = i;
                                }

                            }

                            maximum.setText(String.valueOf(Utils.calculateMinutes((long)test)));
                        }

                        chartSteps = totalSteps;
                    }


                }


                threadHandler.postDelayed(this, 1000);
            }

        };

        startPost = new Runnable() {
            @Override
            public void run() {
                chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffSet);
                chronometer.start();
                Log.d("deb", "Stopwatch Started");
            }
        };
    }

    public void activateSensor(boolean value)
    {
        if (value)
        {
            Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            if(countSensor != null)
            {
                sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
                Log.d("deb", "Sensor is not null");


            }
            else
            {
                Toast.makeText(getActivity(), "SENSOR NOT FOUND", Toast.LENGTH_SHORT).show();
                Log.d("deb", "Sensor is null");

            }
        }

        else
        {
            sensorManager.unregisterListener(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Initial_Count_Key, startingSteps);
            editor.commit();
        }
    }

    public void setButtonListeners()
    {
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent userIntent = new Intent(getActivity(), User.class);
                startActivity(userIntent);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clicked)
                {
                    Log.d("deb", "Clicked start");
                    if (!running)
                    {
                        Log.d("deb", "running is false, starting process");
                        Thread thread = new Thread(startTime);
                        thread.start();
                    }
                    else
                    {
                        Log.d("deb", "Running is true, not doing anything");
                    }


                    if (!updatesOn) {
                        Log.d("deb", "UpdatesOn is False, turning it on.");
                        updatesOn = true;
                        activateSensor(true);
                        mService.requestLocationUpdates();
                    }
                    else
                    {
                        Log.d("deb", "UpdatesOn is True. Not doing anything.");
                    }
                    clicked = true;
                    startButton.setText("Stop");
                }
                else
                {
                    Log.d("deb", "Clicked Stop, removed thread callbacks.");
                    threadHandler.removeCallbacks(startTime);


                    if (running)
                    {
                        Log.d("deb", "Running is true, setting to false. Stopping process. Resetting Timer");
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                pauseOffSet = 0;
                                running = false;
                                threadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        chronometer.stop();
                                    }
                                });
                            }
                        });
                        thread.start();
                    }

                    else
                    {
                        Log.d("deb", "Running is False, not doing anything.");
                    }

                    if (updatesOn) {
                        Log.d("deb", "UpdatesOn is true, turning off live location updates.");
                        updatesOn = false;
                        mService.removeLocationUpdates();
                        activateSensor(false);
                        mService.deleteLoc();

                    }

                    else
                    {
                        Log.d("deb", "updatesOn is false. Not doing anything.");
                    }

                    clicked = false;
                    startButton.setText("Start");
                    addRecord(getView());
                }

            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted do nothing and carry on

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }

                break;

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("deb", "In onPause()");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(myReceiver);
    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
         Log.d("dib", "In onSensorChanged");

        startingSteps = (int) sensorEvent.values[0];
        Log.d("dib", String.valueOf(sensorEvent.values[0]));

        if(!prefs.contains(Initial_Count_Key))
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Initial_Count_Key, startingSteps);
            editor.apply();
        }

        totalSteps = startingSteps - prefs.getInt(Initial_Count_Key, -1);

        }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("deb", "In onReceiver, broadcast received");
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            polylineLocations = intent.getParcelableArrayListExtra("test");

            if (polyline != null)
            {
                Log.d("deb", String.valueOf(polylineLocations));
                polyline.setPoints(polylineLocations);
            }
            else
            {
                polyline = gmap.addPolyline(new PolylineOptions());
                polylineLocations = intent.getParcelableArrayListExtra("test");
                gmap.moveCamera(CameraUpdateFactory.newLatLng(polylineLocations.get(0)));
                polyline.setPoints(polylineLocations);


            }

            if (location != null) {
                Log.d("deb", Utils.getLocationText(location));
                //here = new LatLng(location.getLatitude(), location.getLongitude());

                /*
                if (mark != null )
                {
                    mark.setPosition(here);
                }
                else
                {
                    mark = gmap.addMarker(new MarkerOptions().position(here));
                }
                gmap.moveCamera(CameraUpdateFactory.newLatLng(here));
                */

                /*
                if (polyline!=null)
                {
                    polyline.setPattern();
                }
                else
                {
                    polyline
                }
                */

            }
        }
    }

    public void addRecord(View view)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(
                MyContentProvider.TIME,
                looper);

        float floatRounded = (float) dRounded;

        contentValues.put(
                MyContentProvider.DISTANCE,
                floatRounded
        );

        float floatCalories = (float) caloriesUsed;

        contentValues.put(
                MyContentProvider.CALORIES,
                floatCalories
        );

        Uri uri = getActivity().getContentResolver().insert(
                MyContentProvider.URI,
                contentValues);

    }

    public static class data {

        private int time;
        private int calories;

        public data(int time, int calories)
        {
            this.time = time;
            this.calories = calories;
        }

        public int getValueX()
        {
            return time;
        }

        public int getValueY()
        {
            return calories;
        }
    }
}