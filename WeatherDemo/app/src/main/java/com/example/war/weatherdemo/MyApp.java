package com.example.war.weatherdemo;

import android.app.Application;

import org.litepal.LitePal;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
