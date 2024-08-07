package com.example.ta

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class DataDisplayFragment : Fragment() {

    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data_display, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        database = FirebaseDatabase.getInstance()

        // Load data dynamically from Firebase
        loadData("Temperature", requireView().findViewById(R.id.textViewSuhuValue), "C")
        loadData("Lux", requireView().findViewById(R.id.textViewLuxValue), "Lux")
        loadData("Arus", requireView().findViewById(R.id.textViewArusValue), "mA", true)
        loadData("Daya", requireView().findViewById(R.id.textViewDayaValue), "mW")
    }

    private fun loadData(sensorName: String, textView: TextView, unit: String, multiplyByMinusOne: Boolean = false) {
        val sensorRef = database.reference.orderByChild("Timestamp").limitToLast(1)
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        var value = child.child(sensorName).getValue(Float::class.java) ?: 0f
                        if (multiplyByMinusOne) {
                            value *= -1
                        }
                        val displayText = "$value $unit"
                        textView.text = displayText
                        Log.d("DataDisplayFragment", "$sensorName value: $displayText")
                    }
                } else {
                    Log.e("DataDisplayFragment", "$sensorName node does not exist")
                    textView.text = "Data not available"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataDisplayFragment", "Failed to read $sensorName data", error.toException())
            }
        })
    }
}
