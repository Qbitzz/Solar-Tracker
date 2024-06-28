package com.example.ta

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private lateinit var bluetoothManager: BluetoothManager

    companion object {
        private const val REQUEST_ENABLE_BLUETOOTH = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HomeActivity", "onCreate() called")
        setContentView(R.layout.home)

        // Initialize Firebase
        database = Firebase.database

        // Initialize BluetoothManager
        bluetoothManager = BluetoothManager(this)

        // Check Bluetooth permissions
        bluetoothManager.requestBluetoothPermission()

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
        //Load arus data
        loadLArusData()
        //Load Daya data
        loadLDayaData()
    }

    private fun setupLineChart() {
        val lineChart = findViewById<LineChart>(R.id.line_chart)
        var entries1 = ArrayList<Entry>()
        var entries2 = ArrayList<Entry>()

        // Data for Line 1
        entries1.add(Entry(0f, 10f)) // Start from 0f to align with the current hour
        entries1.add(Entry(1f, 20f))
        entries1.add(Entry(2f, 15f))
        entries1.add(Entry(3f, 25f))
        entries1.add(Entry(4f, 30f))
        entries1.add(Entry(5f, 20f))
        entries1.add(Entry(6f, 18f))
        entries1.add(Entry(7f, 22f))

        // Data for Line 2
        entries2.add(Entry(0f, 15f)) // Example data points for Line 2
        entries2.add(Entry(1f, 25f))
        entries2.add(Entry(2f, 20f))
        entries2.add(Entry(3f, 30f))
        entries2.add(Entry(4f, 35f))
        entries2.add(Entry(5f, 25f))
        entries2.add(Entry(6f, 23f))
        entries2.add(Entry(7f, 27f))

        if (entries1.isNotEmpty() && entries2.isNotEmpty()) {
            // Creating datasets for both lines
            var dataSet1 = LineDataSet(entries1, "Line 1")
            dataSet1.color = Color.GREEN
            dataSet1.valueTextColor = Color.BLACK
            dataSet1.fillAlpha = 80
            dataSet1.setDrawFilled(true)
            dataSet1.fillDrawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(Color.parseColor("#80FFFFFF"), Color.parseColor("#8000FF00")))
            dataSet1.setDrawCircles(true)
            dataSet1.setCircleColor(Color.GREEN)
            dataSet1.circleRadius = 3f
            dataSet1.setDrawValues(true)
            dataSet1.lineWidth = 3f

            var dataSet2 = LineDataSet(entries2, "Line 2")
            dataSet2.color = Color.BLUE
            dataSet2.valueTextColor = Color.BLACK
            dataSet2.fillAlpha = 80
            dataSet2.setDrawFilled(true)
            dataSet2.setDrawCircles(true)
            dataSet2.setCircleColor(Color.BLUE)
            dataSet2.circleRadius = 3f
            dataSet2.setDrawValues(true)
            dataSet2.lineWidth = 3f

            // Creating LineData object with both datasets
            var lineData = LineData(dataSet1, dataSet2)

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
                        val hour = (currentHour - entries1.size + 1 + value.toInt()) % 24
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
        val tempRef = database.reference.child("Temperature")
        tempRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val suhu = snapshot.getValue(Double::class.java)
                    if (suhu != null) {
                        val textViewLuxValue = findViewById<TextView>(R.id.textViewSuhuValue)
                        textViewLuxValue.text = "$suhu C"
                        Log.d("HomeActivity", "Lux value: $suhu")
                    } else {
                        Log.e("HomeActivity", "Failed to read Temperature data: lt is null")
                    }
                } else {
                    Log.e("HomeActivity", "Temperature node does not exist")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Failed to read Temprerature data", error.toException())
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

    private fun loadLArusData() {
        val arusRef = database.reference.child("Arus")
        arusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val arus = snapshot.getValue(Double::class.java)
                    if (arus != null) {
                        val textViewarusValue = findViewById<TextView>(R.id.textViewArusValue)
                        textViewarusValue.text = "$arus A"
                        Log.d("HomeActivity", "arus value: $arus")
                    } else {
                        Log.e("HomeActivity", "Failed to read arus data: lt is null")
                    }
                } else {
                    Log.e("HomeActivity", "arus node does not exist")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Failed to read arus data", error.toException())
            }
        })
    }

    private fun loadLDayaData() {
        val dayaRef = database.reference.child("Daya")
        dayaRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val daya = snapshot.getValue(Double::class.java)
                    if (daya != null) {
                        val textViewdayaValue = findViewById<TextView>(R.id.textViewDayaValue)
                        textViewdayaValue.text = "$daya mW"
                        Log.d("HomeActivity", "daya value: $daya")
                    } else {
                        Log.e("HomeActivity", "Failed to read daya data: lt is null")
                    }
                } else {
                    Log.e("HomeActivity", "daya node does not exist")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Failed to read daya data", error.toException())
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