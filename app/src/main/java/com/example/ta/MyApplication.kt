package com.example.ta

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase with your API key
        FirebaseApp.initializeApp(this, FirebaseOptions.Builder()
            .setApiKey("AIzaSyDfs_JrXdjGe0UdOf6kQyf6DG5MNeZyllw")
            .setApplicationId("esp32-f9fad")
            .setDatabaseUrl("https://esp32-f9fad-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .build())
    }
}