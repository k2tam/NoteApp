package com.example.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import data_local.DataLocalManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setDisplayMode();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toNextActivity();
            }
        },700);
    }

    private void toNextActivity() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void setDisplayMode() {
//        SharedPreferences sharedPreferences = getSharedPreferences("night",0);
        Boolean displayNight = DataLocalManager.getBooleanValue("night_mode");
        Log.d("check",""+displayNight);

        if(displayNight){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }



}