package com.example.ta

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var ssidEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var usernameTextView: TextView
    private lateinit var auth: FirebaseAuth
    private var connectedDevice: BluetoothDevice? = null

    companion object {
        private const val REQUEST_CODE_SELECT_DEVICE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize BluetoothManager
        bluetoothManager = BluetoothManager(this)

        // Request to enable Bluetooth if it's not enabled
        if (!bluetoothManager.isBluetoothEnabled()) {
            bluetoothManager.requestEnableBluetooth(this)
        }

        // Request Bluetooth permissions necessary for your app
        bluetoothManager.requestBluetoothPermissions(this)

        ssidEditText = findViewById(R.id.editTextSSID)
        passwordEditText = findViewById(R.id.editTextPassword)
        usernameTextView = findViewById(R.id.username)

        // Set username from email login
        val currentUser = auth.currentUser
        usernameTextView.text = currentUser?.email ?: "Username"

        findViewById<Button>(R.id.btn_send_wifi_info).setOnClickListener {
            sendWiFiInfo()
        }

        findViewById<Button>(R.id.btn_scan_devices).setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SELECT_DEVICE)
        }

        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Handle bottom navigation buttons
        findViewById<Button>(R.id.btn_home).setOnClickListener { switchToHomeLayout() }
        findViewById<Button>(R.id.btn_control).setOnClickListener { switchToControlLayout() }
        findViewById<Button>(R.id.btn_profile).setOnClickListener { switchToProfileLayout() }
    }

    private fun sendWiFiInfo() {
        val ssid = ssidEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (ssid.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "SSID and Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val messageArray = arrayOf(ssid, password).joinToString(";").toByteArray()
        if (connectedDevice != null) {
            bluetoothManager.sendBytes(messageArray)
            Toast.makeText(this, "WiFi info sent", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No device connected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_DEVICE && resultCode == Activity.RESULT_OK) {
            val deviceAddress = data?.getStringExtra("device_address")
            deviceAddress?.let {
                connectedDevice = bluetoothManager.getPairedDevices()?.firstOrNull { it.address == deviceAddress }
                connectedDevice?.let {
                    if (bluetoothManager.connectToDevice(it)) {
                        Toast.makeText(this, "Connected to ${it.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to connect to ${it.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun switchToHomeLayout() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun switchToControlLayout() {
        startActivity(Intent(this, ControlActivity::class.java))
        finish()
    }

    private fun switchToProfileLayout() {
        // Current activity, no need to switch
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.disconnect()
    }
}
