package com.example.ta

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase with your API key
        FirebaseApp.initializeApp(this, FirebaseOptions.Builder()
            .setApiKey("")
            .setApplicationId("")
            .setProjectId("")
            .setDatabaseUrl("")
            .build())

    }
}