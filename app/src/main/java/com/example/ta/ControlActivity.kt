package com.example.ta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ControlActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Setup buttons in the control.xml layout
        setupControlButtons()

        // Retrieve data from Firebase
        fetchSumbuData()
    }

    private fun fetchSumbuData() {
        val sumbuXValue = findViewById<TextView>(R.id.sumbu_x_value)
        val sumbuYValue = findViewById<TextView>(R.id.sumbu_y_value)

        database.addValueEventListener(object : ValueEventListener {
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
        })
    }

    private fun setupControlButtons() {
        findViewById<Button>(R.id.btn_profile).setOnClickListener(this)
        findViewById<Button>(R.id.btn_back).setOnClickListener(this)
        findViewById<Button>(R.id.btn_home).setOnClickListener(this)
        findViewById<Button>(R.id.btn_result).setOnClickListener(this)
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
            R.id.btn_result -> switchToResultLayout()
            R.id.btn_control -> switchToControlLayout()
            R.id.atas -> updateServo("Servo X", 5)
            R.id.bawah -> updateServo("Servo X", -5)
            R.id.kiri -> updateServo("Servo Y", 5)
            R.id.kanan -> updateServo("Servo Y", -5)
        }
    }

    private fun updateServo(servo: String, value: Int) {
        database.child("solarTracker/control").child(servo).setValue(value)
            .addOnSuccessListener {
                showToast("Updated $servo with value $value")
            }
            .addOnFailureListener {
                showToast("Failed to update $servo: ${it.message}")
            }
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
        // Current activity , no need to switch
    }

    private fun switchToResultLayout() {
        // Add logic to switch to the result activity
        // startActivity(Intent(this, ResultActivity::class.java))
        // finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
