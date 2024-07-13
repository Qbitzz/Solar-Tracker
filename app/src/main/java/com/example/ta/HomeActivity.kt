package com.example.ta

import android.app.DatePickerDialog
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var lineChart: LineChart
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Initialize Firebase
        database = Firebase.database

        // Initialize BluetoothManager
        bluetoothManager = BluetoothManager(this)

        // Request to enable Bluetooth if it's not enabled
        if (!bluetoothManager.isBluetoothEnabled()) {
            bluetoothManager.requestEnableBluetooth(this)
        }

        // Request Bluetooth permissions necessary for your app
        bluetoothManager.requestBluetoothPermissions(this)

        setupHomeButtons()
        setupLineChart()

        // Load data dynamically from Firebase
        loadData("Temperature", R.id.textViewSuhuValue, "C")
        loadData("Lux", R.id.textViewLuxValue, "Lux")
        loadData("Arus", R.id.textViewArusValue, "A")
        loadData("Daya", R.id.textViewDayaValue, "mW")

        // Setup Date Picker Button
        findViewById<Button>(R.id.btn_select_date).setOnClickListener {
            showDatePickerDialog()
        }

        // Load current date data by default
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadChartData(selectedDate)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = String.format(
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )
                loadChartData(selectedDate)
            },
            year, month, day
        ).show()
    }

    private fun setupLineChart() {
        lineChart = findViewById(R.id.line_chart)
        val entries = ArrayList<Entry>()

        val dataSet = LineDataSet(entries, "Sensor Data")
        configureDataSet(dataSet, Color.GREEN)

        lineChart.data = LineData(dataSet)
        configureLineChart()
    }

    private fun configureDataSet(dataSet: LineDataSet, color: Int) {
        dataSet.color = color
        dataSet.valueTextColor = Color.BLACK
        dataSet.fillAlpha = 80
        dataSet.setDrawFilled(true)
        dataSet.fillDrawable = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(Color.parseColor("#80FFFFFF"), color)
        )
        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(color)
        dataSet.circleRadius = 3f
        dataSet.setDrawValues(true)
        dataSet.lineWidth = 3f
    }

    private fun configureLineChart() {
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.setPinchZoom(true)
        lineChart.setDoubleTapToZoomEnabled(true)
        lineChart.setTouchEnabled(true)
        lineChart.xAxis.apply {
            isEnabled = true
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            labelCount = 8
            setGranularity(1f)
            textColor = Color.BLACK
            textSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val hour = value.toInt() % 24
                    return String.format("%02d:00", hour)
                }
            }
        }
        lineChart.axisLeft.apply {
            isEnabled = true
            setDrawGridLines(true)
            textColor = Color.BLACK
            textSize = 12f
        }
        lineChart.axisRight.isEnabled = false
    }

    private fun loadChartData(date: String?) {
        val dayaRef = database.reference
        lifecycleScope.launch {
            val entries = ArrayList<Entry>()
            val hourlyData = withContext(Dispatchers.IO) {
                val data = mutableMapOf<Int, MutableList<Float>>()
                val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

                dayaRef.get().addOnSuccessListener { snapshot ->
                    snapshot.children.forEach { child ->
                        val key = child.key
                        if (key != null && key.matches(Regex("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}"))) {
                            try {
                                val timestamp = formatter.parse(key)
                                val dayaValue = child.child("Daya").getValue(Float::class.java)
                                if (timestamp != null && dayaValue != null) {
                                    val calendar = Calendar.getInstance()
                                    calendar.time = timestamp
                                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                                    val dataDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp)

                                    if (date == null || dataDate == date) {
                                        data.getOrPut(hour) { mutableListOf() }.add(dayaValue)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("HomeActivity", "Error parsing data", e)
                            }
                        }
                    }
                }.await()
                data
            }

            entries.clear()
            hourlyData.entries.sortedBy { it.key }.forEach { entry ->
                val averageValue = entry.value.sum() / entry.value.size
                entries.add(Entry(entry.key.toFloat(), averageValue))
            }

            val dataSet = (lineChart.data.getDataSetByIndex(0) as LineDataSet).apply {
                values = entries
            }

            dataSet.notifyDataSetChanged()
            lineChart.data.notifyDataChanged()
            lineChart.notifyDataSetChanged()
            lineChart.invalidate()
        }
    }

    private fun loadData(sensorName: String, textViewId: Int, unit: String) {
        val sensorRef = database.reference.child(sensorName)
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val value = snapshot.value
                    val displayText = when (value) {
                        is Long -> "$value $unit"
                        is Double -> "$value $unit"
                        is String -> value
                        else -> "Invalid data"
                    }
                    updateTextView(textViewId, displayText)
                    Log.d("HomeActivity", "$sensorName value: $displayText")
                } else {
                    Log.e("HomeActivity", "$sensorName node does not exist")
                    updateTextView(textViewId, "Data not available")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Failed to read $sensorName data", error.toException())
            }
        })
    }

    private fun updateTextView(textViewId: Int, text: String) {
        findViewById<TextView>(textViewId).text = text
    }

    private fun setupHomeButtons() {
        findViewById<Button>(R.id.btn_home).setOnClickListener { switchToHomeLayout() }
        findViewById<Button>(R.id.btn_control).setOnClickListener { switchToControlLayout() }
        findViewById<Button>(R.id.btn_profile).setOnClickListener { switchToProfileLayout() }
        findViewById<Button>(R.id.btn_result).setOnClickListener { switchToResultLayout() }
    }

    private fun switchToHomeLayout() {
        // Handle switching to home layout if needed
    }

    private fun switchToControlLayout() {
        startActivity(Intent(this, ControlActivity::class.java))
        finish()
    }

    private fun switchToProfileLayout() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    private fun switchToResultLayout() {
        // Start result activity, implement if necessary
    }

    // Bluetooth Management
    fun connectToDevice(device: BluetoothDevice): Boolean {
        return bluetoothManager.connectToDevice(device)
    }

    fun sendMessage(message: String) {
        bluetoothManager.sendMessage(message)
    }

    fun disconnectDevice() {
        bluetoothManager.disconnect()
    }
}
