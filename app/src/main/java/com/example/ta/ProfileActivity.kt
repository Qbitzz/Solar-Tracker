package com.example.ta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize BluetoothManager
        bluetoothManager = BluetoothManager(this)

        // Set up buttons
        setupProfileButtons()

        // Display the current user's email
        displayUserEmail()
    }

    private fun setupProfileButtons() {
        findViewById<Button>(R.id.btn_home).setOnClickListener(this)
        findViewById<Button>(R.id.btn_result).setOnClickListener(this)
        findViewById<Button>(R.id.btn_control).setOnClickListener(this)
        findViewById<Button>(R.id.btn_profile).setOnClickListener(this)
        findViewById<Button>(R.id.btn_back).setOnClickListener(this)
        findViewById<Button>(R.id.buttonLogout).setOnClickListener(this)
        findViewById<Button>(R.id.btn_send_wifi_info).setOnClickListener(this) // New button for sending WiFi info
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_home -> switchToHomeLayout()
            R.id.btn_result -> switchToResultLayout()
            R.id.btn_control -> switchToControlLayout()
            R.id.btn_profile -> switchToProfileLayout()
            R.id.btn_back -> switchToHomeLayout()
            R.id.buttonLogout -> logoutUser()
            R.id.btn_send_wifi_info -> sendWifiInfo() // New case for sending WiFi info
        }
    }

    private fun switchToHomeLayout() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun switchToResultLayout() {
        // Add logic to switch to the result activity
        // startActivity(Intent(this, ResultActivity::class.java))
        // finish()
    }

    private fun switchToControlLayout() {
        startActivity(Intent(this, ControlActivity::class.java))
        finish()
    }

    private fun switchToProfileLayout() {
        // Current activity, no need to switch
    }

    private fun logoutUser() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun displayUserEmail() {
        val user = auth.currentUser
        user?.let {
            val email = user.email
            findViewById<TextView>(R.id.username).text = email
        }
    }

    // New function to send WiFi info
    private fun sendWifiInfo() {
        val ssid = findViewById<EditText>(R.id.editTextSSID).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
        if (ssid.isNotEmpty() && password.isNotEmpty()) {
            if (bluetoothManager.isBluetoothEnabled()) {
                bluetoothManager.sendMessage("SSID:$ssid")
                bluetoothManager.sendMessage("Password:$password")
                Toast.makeText(this, "WiFi info sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter both SSID and Password", Toast.LENGTH_SHORT).show()
        }
    }
}
