package com.example.ta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ControlActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var database: DatabaseReference
    private lateinit var modeSwitch: Switch
    private var isManualMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Setup buttons and switch in the control.xml layout
        setupControlButtons()
        setupModeSwitch()

        // Retrieve data from Firebase
        fetchSumbuData()
        fetchMode()
    }

    private fun setupModeSwitch() {
        modeSwitch = findViewById(R.id.switch_mode)
        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleMode(isChecked)
        }
    }

    private fun fetchSumbuData() {
        val sumbuXValue = findViewById<TextView>(R.id.sumbu_x_value)
        val sumbuYValue = findViewById<TextView>(R.id.sumbu_y_value)

        // Listener for servo data
        val servoDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val servoX = dataSnapshot.child("Servo X").getValue(Int::class.java) ?: 0
                    val servoY = dataSnapshot.child("Servo Y").getValue(Int::class.java) ?: 0

                    sumbuXValue.text = servoX.toString()
                    sumbuYValue.text = servoY.toString()
                } else {
                    sumbuXValue.text = "0"
                    sumbuYValue.text = "0"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, "Failed to load data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Attach the listener to the 'control' node in the database
        database.child("control").addValueEventListener(servoDataListener)
    }

    private fun fetchMode() {
        database.child("control/mode").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                isManualMode = dataSnapshot.getValue(Boolean::class.java) ?: false
                modeSwitch.isChecked = !isManualMode
                updateModeSwitchText(isManualMode)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, "Failed to load mode: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupControlButtons() {
        findViewById<Button>(R.id.btn_profile).setOnClickListener(this)
        findViewById<Button>(R.id.btn_back).setOnClickListener(this)
        findViewById<Button>(R.id.btn_home).setOnClickListener(this)
        findViewById<Button>(R.id.btn_control).setOnClickListener(this)

        // Find and Set a click listener LinearLayout with id atas
        findViewById<LinearLayout>(R.id.atas).setOnClickListener(this)

        // Find and Set a click listener ImageView with id kiri
        findViewById<ImageView>(R.id.kiri).setOnClickListener(this)

        // Find and Set a click listener ImageView with id kanan
        findViewById<ImageView>(R.id.kanan).setOnClickListener(this)

        // Find and Set a click listener LinearLayout with id bawah
        findViewById<LinearLayout>(R.id.bawah).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_profile -> switchToProfileLayout()
            R.id.btn_back -> switchToHomeLayout()
            R.id.btn_home -> switchToHomeLayout()
            R.id.btn_control -> switchToControlLayout()
            R.id.atas -> {
                if (isManualMode) adjustServo("TempY", 5, 20, 120)
                else showToast("Manual control is disabled")
            }
            R.id.bawah -> {
                if (isManualMode) adjustServo("TempY", -5, 20, 120)
                else showToast("Manual control is disabled")
            }
            R.id.kiri -> {
                if (isManualMode) adjustServo("TempX", -5, 40, 140)
                else showToast("Manual control is disabled")
            }
            R.id.kanan -> {
                if (isManualMode) adjustServo("TempX", 5, 40, 140)
                else showToast("Manual control is disabled")
            }
        }
    }

    private fun adjustServo(servo: String, adjustment: Int, minValue: Int, maxValue: Int) {
        val currentValueRef = database.child("control").child(servo)
        currentValueRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentValue = dataSnapshot.getValue(Int::class.java) ?: 0
                val newValue = (currentValue + adjustment).coerceIn(minValue, maxValue)
                currentValueRef.setValue(newValue)
                    .addOnSuccessListener {
                        showToast("Updated $servo to $newValue")
                    }
                    .addOnFailureListener {
                        showToast("Failed to update $servo: ${it.message}")
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("Failed to load data: ${databaseError.message}")
            }
        })
    }

    private fun toggleMode(isAutoMode: Boolean) {
        val isManualMode = !isAutoMode
        database.child("control/mode").setValue(isManualMode)
            .addOnSuccessListener {
                updateModeSwitchText(isManualMode)
                this.isManualMode = isManualMode
            }
            .addOnFailureListener {
                showToast("Failed to toggle mode: ${it.message}")
            }
    }

    private fun updateModeSwitchText(isManualMode: Boolean) {
        modeSwitch.text = if (isManualMode) "Manual" else "Auto"
    }

    private fun switchToProfileLayout() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    private fun switchToHomeLayout() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun switchToControlLayout() {
        // Current activity, no need to switch
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
