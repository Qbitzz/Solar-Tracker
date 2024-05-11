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

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Setup buttons in the main.xml layout
        setupHomeButtons()

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
        val textViewLuxValue = findViewById<TextView>(R.id.textViewLuxValue)
        textViewLuxValue.text = "500" // Replace with your dummy data
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