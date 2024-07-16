package com.example.ta

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class LineChartFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var lineChart: LineChart
    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_line_chart, container, false)
        lineChart = view.findViewById(R.id.line_chart)
        setupLineChart()
        setupDatePicker(view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        database = FirebaseDatabase.getInstance()

        // Load current date data by default
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadChartData(selectedDate)
    }

    private fun setupLineChart() {
        val entries = ArrayList<Entry>()
        val dataSet = LineDataSet(entries, "Daya miliwatt")
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
            granularity = 1f
            textColor = Color.BLACK
            textSize = 12f
            valueFormatter = HourAxisValueFormatter()
        }
        lineChart.axisLeft.apply {
            isEnabled = true
            setDrawGridLines(true)
            textColor = Color.BLACK
            textSize = 12f
        }
        lineChart.axisRight.isEnabled = false
    }

    private fun setupDatePicker(view: View) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        view.findViewById<Button>(R.id.btn_select_date).setOnClickListener {
            DatePickerDialog(
                requireContext(),
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
    }

    private fun loadChartData(date: String?) {
        val dayaRef = database.reference
        lifecycleScope.launch {
            val entries = ArrayList<Entry>()
            val hourlyData = withContext(Dispatchers.IO) {
                val data = mutableMapOf<Int, MutableList<Float>>()
                val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

                val snapshot = dayaRef.get().await()
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
                            Log.e("LineChartFragment", "Error parsing data", e)
                        }
                    }
                }
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

    // ValueFormatter for 24-hour format
    class HourAxisValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val hour = value.toInt()
            return String.format("%02d:00", hour)
        }
    }
}
