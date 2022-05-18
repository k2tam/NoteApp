package com.example.noteapp;

import android.app.Application;

import data_local.DataLocalManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataLocalManager.init(getApplicationContext());

        if(!DataLocalManager.getFirstInstalled()){
//            First time install
            DataLocalManager.setPrefFirstInstalled(true);
        }
    }
}
