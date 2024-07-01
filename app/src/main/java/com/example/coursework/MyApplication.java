package com.example.coursework;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Включение оффлайн-поддержки Firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
