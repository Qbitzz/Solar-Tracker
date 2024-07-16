package com.example.ta

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity() {

    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Initialize BluetoothManager
        bluetoothManager = BluetoothManager(this)

        // Request Bluetooth permissions and enable Bluetooth if necessary
        requestBluetoothPermissionsAndEnable()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LineChartFragment())
                .commitNow()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_data, DataDisplayFragment())
                .commitNow()
        }

        setupHomeButtons()
    }

    private fun setupHomeButtons() {
        findViewById<Button>(R.id.btn_home).setOnClickListener { switchToHomeLayout() }
        findViewById<Button>(R.id.btn_control).setOnClickListener { switchToControlLayout() }
        findViewById<Button>(R.id.btn_profile).setOnClickListener { switchToProfileLayout() }
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

    private fun requestBluetoothPermissionsAndEnable() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            enableBluetooth()
        }
    }

    private val requestBluetoothPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
                permissions[Manifest.permission.BLUETOOTH_SCAN] == true) {
                enableBluetooth()
            } else {
                Toast.makeText(this, "Bluetooth permissions are required for this app", Toast.LENGTH_SHORT).show()
            }
        }

    private fun enableBluetooth() {
        if (!bluetoothManager.isBluetoothEnabled()) {
            bluetoothManager.requestEnableBluetooth(this)
        }
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
