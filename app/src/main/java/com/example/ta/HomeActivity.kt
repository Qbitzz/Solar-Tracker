package com.example.ta

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HomeActivity", "onCreate() called")
        setContentView(R.layout.home)

        // Initialize Firebase
        database = Firebase.database

        // Check if the database variable is not null
        if (database == null) {
            Log.e("HomeActivity", "Firebase Realtime Database is not initialized")
        } else {
            Log.d("HomeActivity", "Firebase Realtime Database is initialized")
        }

        // Setup buttons in the main.xml layout
        setupHomeButtons()

        // Load Lux data from Firebase
        loadLuxData()

        // Check if the application is connected to Firebase
                if (FirebaseApp.getInstance() == null) {
                    // Application is not connected to Firebase
                    Log.e("HomeActivity", "Application is not connected to Firebase")
                } else {
                    // Application is connected to Firebase
                    Log.d("HomeActivity", "Application is connected to Firebase")
                }

        // Find the LineChart view
        val lineChart = findViewById<LineChart>(R.id.line_chart)

        // Create a list of entries (x,y) for your chart
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 10f))
        entries.add(Entry(1f, 20f))
        entries.add(Entry(2f, 15f))
        entries.add(Entry(3f, 25f))
        entries.add(Entry(4f, 30f))
        entries.add(Entry(5f, 20f))

        // Check if the entries list is not empty
        if (entries.isNotEmpty()) {
            // Create a dataset from the entries
            val dataSet = LineDataSet(entries, "Energy Gain Per Hour")

            // Set some styling options for the dataset
            dataSet.color = Color.BLUE
            dataSet.valueTextColor = Color.BLACK

            // Create a LineData object with the dataset
            val lineData = LineData(dataSet)

            // Set the data to the chart
            lineChart.data = lineData

            // Refresh the chart
            lineChart.invalidate()
        } else {
            // Handle the case when the entries list is empty
            Log.e("HomeActivity", "No data available for the chart")
        }

        // dummy data for textViewArusValue
        val textViewArusValue = findViewById<TextView>(R.id.textViewArusValue)
        textViewArusValue.text = "100 A" // Replace with your dummy data

        // Set dummy data for textViewSuhuValue
        val textViewSuhuValue = findViewById<TextView>(R.id.textViewSuhuValue)
        textViewSuhuValue.text = "25" // Replace with your dummy data

        // Set dummy data for textViewDayaValue
        val textViewDayaValue = findViewById<TextView>(R.id.textViewDayaValue)
        textViewDayaValue.text = "120 W" // Replace with your dummy data

        // Set dummy data for textViewLuxValue
        //val textViewLuxValue = findViewById<TextView>(R.id.textViewLuxValue)
        //textViewLuxValue.text = "500" // Replace with your dummy data
    }

    private fun loadLuxData() {
        val luxRef = database.reference.child("ldr").child("lt")
        luxRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val lt = snapshot.value as? Long
                    if (lt != null) {
                        // Find the TextView for LuxValue
                        val textViewLuxValue = findViewById<TextView>(R.id.textViewLuxValue)

                        // Update the TextView with the Lux value
                        textViewLuxValue.text = "$lt Lux"

                        // Log the Lux value for debugging
                        Log.d("HomeActivity", "Lux value: $lt")
                    } else {
                        Log.e("HomeActivity", "Failed to read Lux data: lt is null")
                    }
                } else {
                    Log.e("HomeActivity", "Lux node does not exist")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Failed to read Lux data", error.toException())
            }
        })
    }


    private fun setupHomeButtons() {
        val btnControl = findViewById<Button>(R.id.btn_control)
        btnControl.setOnClickListener {
            // Switch to control layout
            switchToControlLayout()
        }
    }

    private fun switchToControlLayout() {
        startActivity(Intent(this, ControlActivity::class.java))
        finish()
    }
}