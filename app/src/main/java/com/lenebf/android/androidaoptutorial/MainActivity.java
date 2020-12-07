package com.lenebf.android.androidaoptutorial;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lenebf.android.buildtime.BuildTime;
import com.lenebf.android.buildtime_annotation.KeepBuildTime;
import com.lenebf.android.mlog.MethodLoggable;

@KeepBuildTime
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testMethod("lenebf", 10);
        Log.d("lenebf", "The build time is " + BuildTime.get(this));
    }

    @MethodLoggable
    private void testMethod(String name, int age) {
        int i = 10;
        int j = age + i;
    }
}
