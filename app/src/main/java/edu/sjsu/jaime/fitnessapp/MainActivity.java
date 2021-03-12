package edu.sjsu.jaime.fitnessapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity{

    FragmentManager fragmentManager;
    private portrait portraitFragment = new portrait();
    private BlankFragment blankFragment = new BlankFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("dab", "In activity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration config = getResources().getConfiguration();
        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();


        portraitFragment = new portrait();
        blankFragment = new BlankFragment();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            fragmentTransaction.replace(android.R.id.content, portraitFragment);
            fragmentTransaction.add(android.R.id.content, blankFragment);
            fragmentTransaction.hide(blankFragment);
        } else {
            fragmentTransaction.replace(android.R.id.content, blankFragment);
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            Log.d("late", "in landscape");

            fragmentTransaction.hide(portraitFragment);
            fragmentTransaction.show(blankFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
        else
        {
            fragmentTransaction.hide(blankFragment);
            fragmentTransaction.show(portraitFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d("dab", "in activity onDestroy");
    }
}
