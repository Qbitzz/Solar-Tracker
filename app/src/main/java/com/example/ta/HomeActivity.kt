package com.example.ta

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity() {

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            // Dark mode is enabled
        } else {
            // Light mode is enabled
        }
    }

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

        //Load Temp Data
        loadtempdata()

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
        entries.add(Entry(0f, 10f)) // Start from 0f to align with the current hour
        entries.add(Entry(1f, 20f))
        entries.add(Entry(2f, 15f))
        entries.add(Entry(3f, 25f))
        entries.add(Entry(4f, 30f))
        entries.add(Entry(5f, 20f)) // Add more entries if needed

// Check if the entries list is not empty
        if (entries.isNotEmpty()) {
            // Create a dataset from the entries
            val dataSet = LineDataSet(entries, "Watt Gain Per Hour")

            // Apply gradient fill
            dataSet.setDrawFilled(true)
            dataSet.color = Color.GREEN
            dataSet.valueTextColor = Color.BLACK
            dataSet.fillAlpha = 80

            // Create gradient fill with green color
            val startColor = Color.parseColor("#80FFFFFF") // Transparent white
            val endColor = Color.parseColor("#8000FF00")   // Transparent green
            val gradientColors = intArrayOf(startColor, endColor)
            val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, gradientColors)

            // Set gradient fill
            dataSet.fillDrawable = gradientDrawable

            // Customize marker
            dataSet.setDrawCircles(true)
            dataSet.setCircleColor(Color.GREEN)
            dataSet.circleRadius = 3f // Adjust circle size
            dataSet.setDrawValues(true) // Hide values on markers

            // Adjust the width of the line
            dataSet.lineWidth = 3f // Set the desired width here

            // Create a LineData object with the dataset
            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // Customize the appearance of the chart
            lineChart.apply {
                // Remove description
                description.isEnabled = false
                // Remove legend
                legend.isEnabled = false
                // Disable zoom
                setPinchZoom(false)
                // Disable double tap to zoom
                setDoubleTapToZoomEnabled(false)
                // Disable all interactions
                setTouchEnabled(false)
            }

            // Get current hour in 24-hour format
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            // Check if the app is in dark mode
            val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

            lineChart.xAxis.apply {
                // Enable X axis
                isEnabled = true
                // Disable grid lines
                setDrawGridLines(false)
                // Set position to bottom
                position = XAxis.XAxisPosition.BOTTOM
                // Reduce label count to avoid duplicates
                labelCount = 10 // You can adjust this value as needed
                // Enable granularity to prevent duplicate labels
                setGranularityEnabled(true)
                // Set label text color based on dark mode
                textColor = if (isDarkMode) Color.WHITE else Color.BLACK
                // Set label text size
                textSize = 12f
                // Set value formatter to display hours
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        // Calculate the hour based on the current hour and the value offset
                        val hour = (currentHour - entries.size + 1 + value.toInt()) % 24
                        // Ensure the hour wraps around correctly
                        val displayHour = if (hour < 0) hour + 24 else hour
                        // Return the formatted hour
                        return "%02d:00".format(displayHour)
                    }
                }
            }

            // Customize the Y axis (left axis)
            lineChart.axisLeft.apply {
                // Enable Y axis
                isEnabled = true
                // Disable grid lines
                setDrawGridLines(false)
                // Set label text color based on dark mode
                textColor = if (isDarkMode) Color.WHITE else Color.BLACK
                // Set label text size
                textSize = 12f
                // Set value formatter to display watt
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}W"
                    }
                }
            }
            // Disable the right Y axis
            lineChart.axisRight.isEnabled = false

            // Refresh the chart
            lineChart.invalidate()
        } else {
            // Handle the case when the entries list is empty
            Log.e("HomeActivity", "No data available for the chart")
        }

        // dummy data for textViewArusValue
        val textViewArusValue = findViewById<TextView>(R.id.textViewArusValue)
        textViewArusValue.text = "100 A" // Replace with your dummy data

        // Set dummy data for textViewDayaValue
        val textViewDayaValue = findViewById<TextView>(R.id.textViewDayaValue)
        textViewDayaValue.text = "120 W" // Replace with your dummy data
    }

    private fun loadtempdata() {
        val tempRef = database.reference.child("temperature")
        tempRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temperature = snapshot.getValue(Double::class.java)
                if (temperature != null) {
                    // Find the TextView for Temperature Value
                    val textViewSuhuValue = findViewById<TextView>(R.id.textViewSuhuValue)
                    // Update the TextView with the Temperature value
                    textViewSuhuValue.text = "$temperature C"
                    // Log the Temperature value for debugging
                    Log.d("HomeActivity", "Temperature value: $temperature")
                } else {
                    Log.e("HomeActivity", "Failed to read temperature data: temperature is null")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Failed to read temperature data", error.toException())
            }
        })
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