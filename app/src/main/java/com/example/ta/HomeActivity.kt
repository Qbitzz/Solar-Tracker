package com.example.ta

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
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
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HomeActivity", "onCreate() called")
        setContentView(R.layout.home)

        // Initialize Firebase
        database = Firebase.database

        // Check if the application is connected to Firebase
        if (FirebaseApp.getInstance() == null) {
            // Application is not connected to Firebase
            Log.e("HomeActivity", "Application is not connected to Firebase")
        } else {
            // Application is connected to Firebase
            Log.d("HomeActivity", "Application is connected to Firebase")
        }

        // Setup buttons in the main.xml layout
        setupHomeButtons()
        // Load Lux data from Firebase
        loadLuxData()
        // Load Temperature data
        loadTempData()
        // Setup LineChart
        setupLineChart()
    }

    private fun setupLineChart() {
        val lineChart = findViewById<LineChart>(R.id.line_chart)
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 10f)) // Start from 0f to align with the current hour
        entries.add(Entry(1f, 20f))
        entries.add(Entry(2f, 15f))
        entries.add(Entry(3f, 25f))
        entries.add(Entry(4f, 30f))
        entries.add(Entry(5f, 20f)) // Add more entries if needed

        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Watt Gain Per Hour")
            dataSet.setDrawFilled(true)
            dataSet.color = Color.GREEN
            dataSet.valueTextColor = Color.BLACK
            dataSet.fillAlpha = 80

            val startColor = Color.parseColor("#80FFFFFF") // Transparent white
            val endColor = Color.parseColor("#8000FF00")   // Transparent green
            val gradientColors = intArrayOf(startColor, endColor)
            val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, gradientColors)
            dataSet.fillDrawable = gradientDrawable

            dataSet.setDrawCircles(true)
            dataSet.setCircleColor(Color.GREEN)
            dataSet.circleRadius = 3f
            dataSet.setDrawValues(true)

            dataSet.lineWidth = 3f

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            lineChart.apply {
                description.isEnabled = false
                legend.isEnabled = false
                setPinchZoom(true)
                setDoubleTapToZoomEnabled(false)
                setTouchEnabled(true)
            }

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

            lineChart.xAxis.apply {
                isEnabled = true
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                labelCount = 10
                setGranularityEnabled(true)
                textColor = if (isDarkMode) Color.WHITE else Color.BLACK
                textSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val hour = (currentHour - entries.size + 1 + value.toInt()) % 24
                        val displayHour = if (hour < 0) hour + 24 else hour
                        return "%02d:00".format(displayHour)
                    }
                }
            }

            lineChart.axisLeft.apply {
                isEnabled = true
                setDrawGridLines(false)
                textColor = if (isDarkMode) Color.WHITE else Color.BLACK
                textSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}W"
                    }
                }
            }
            lineChart.axisRight.isEnabled = false

            lineChart.invalidate()
        } else {
            Log.e("HomeActivity", "No data available for the chart")
        }
    }

    private fun loadTempData() {
        val tempRef = database.reference.child("temperature")
        tempRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temperature = snapshot.getValue(Double::class.java)
                if (temperature != null) {
                    val textViewSuhuValue = findViewById<TextView>(R.id.textViewSuhuValue)
                    textViewSuhuValue.text = "$temperature C"
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
        val luxRef = database.reference.child("Lux")
        luxRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val lux = snapshot.getValue(Long::class.java)
                    if (lux != null) {
                        val textViewLuxValue = findViewById<TextView>(R.id.textViewLuxValue)
                        textViewLuxValue.text = "$lux Lux"
                        Log.d("HomeActivity", "Lux value: $lux")
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
            switchToControlLayout()
        }
    }
    private fun switchToControlLayout() {
        startActivity(Intent(this, ControlActivity::class.java))
        finish()
    }
}