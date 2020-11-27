package com.lenebf.android.androidaoptutorial;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lenebf.android.mlog.MethodLoggable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        testMethod("lenebf", 10);
    }

    @MethodLoggable
    private void testMethod(String name, int age) {
        int i = 10;
        int j = age + i;
    }
}
