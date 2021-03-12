package edu.sjsu.jaime.fitnessapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class User extends AppCompatActivity {
    private EditText nameText;
    private EditText weightText;
    private Spinner spinner;
    private int temp;
    private Button butt;

    public static double distanceRounded;
    public static double weekdistRounded;

    private static final String gender_key = "Gender";
    private static final String name_key = "Name";
    private static final String weight_key = "weight";
    private SharedPreferences prefs;


    public static int timeTotal=0;
    public static double distanceTotal=0;
    public static int caloriesTotal=0;

    public static TextView distance;
    public static TextView allDistance;
    public static TextView amount;
    public static TextView allAmount;
    public static TextView time;
    public static TextView allTime;
    public static TextView calories;
    public static TextView allCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(!prefs.contains(name_key))
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(name_key, "John Doe");
            editor.apply();
        }

        setContentView(R.layout.activity_user);

        distance = findViewById(R.id.weekDist2);
        allDistance = findViewById(R.id.allDistance2);
        amount = findViewById(R.id.weekWorkout2);
        allAmount = findViewById(R.id.allWorkouts2);
        time = findViewById(R.id.weekTime2);
        allTime = findViewById(R.id.allTime2);
        calories = findViewById(R.id.weekCal2);
        allCalories = findViewById(R.id.allCal2);
        butt = findViewById(R.id.button);


        spinner = findViewById(R.id.spinner);
        weightText = findViewById(R.id.weightText);
        nameText = findViewById(R.id.nameText2);

        nameText.setText(prefs.getString(name_key, "John Doe"));

        weightText.setText(String.valueOf(prefs.getInt(weight_key, 150)));


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("M");
        arrayList.add("W");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);

        spinner.setAdapter(adapter);

        if (prefs.getString(gender_key, "M") == "M")
        {
            spinner.setSelection(0);
        }
        else
        {
            spinner.setSelection(1);
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0)
                {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(gender_key, "M");
                    editor.apply();
                }
                else
                {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(gender_key, "W");
                    editor.apply();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        nameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(name_key, String.valueOf(charSequence));
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        weightText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals(""))
                {
                    temp = 0;
                }
                else
                {
                    temp = Integer.valueOf(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(weight_key, temp);
                editor.apply();
            }
        });

        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        retrieveRecord();
    }

    public void retrieveRecord()
    {
        timeTotal=0;
        distanceTotal=0;
        caloriesTotal=0;
        int workoutTotal = 0;

        String URL = "content://com.wearable.myprovider/workouts";
        Uri workouts = Uri.parse(URL);
        Cursor c = managedQuery(workouts, null, null, null, "_id");

        if (c.moveToFirst())
        {
            workoutTotal = c.getCount();
            do {
                caloriesTotal += c.getInt(c.getColumnIndex(MyContentProvider.CALORIES));
                distanceTotal += c.getDouble(c.getColumnIndex(MyContentProvider.DISTANCE));
                timeTotal += c.getInt(c.getColumnIndex(MyContentProvider.TIME));
            }
            while (c.moveToNext());
        }

        distanceRounded = Math.round(distanceTotal*100.0)/100.0;
        weekdistRounded = Math.round((distanceRounded/workoutTotal)*100.0)/100.0;

        distance.setText(String.valueOf(weekdistRounded));
        allDistance.setText(String.valueOf(distanceRounded));

        time.setText(Utils.calculateTime((long) timeTotal/5));
        allTime.setText(Utils.calculateTime((long) timeTotal));

        calories.setText(String.valueOf(caloriesTotal/workoutTotal));
        allCalories.setText(String.valueOf(caloriesTotal));

        amount.setText(String.valueOf(workoutTotal/workoutTotal));
        allAmount.setText(String.valueOf(workoutTotal));
    }

}
