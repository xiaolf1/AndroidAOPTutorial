package com.lenebf.android.androidaoptutorial;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "lenebf";

     @Override
     protected void onPause() {
         super.onPause();
         Log.d(TAG, getClass().getSimpleName() + ": onPause");
     }

    @Override
    protected void onResume() {
        Log.d(TAG, getClass().getSimpleName() + ": onResume");
        super.onResume();
    }
}
