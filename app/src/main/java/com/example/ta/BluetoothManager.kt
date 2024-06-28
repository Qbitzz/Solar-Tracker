package com.example.ta

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.app.Activity
import android.Manifest

class BluetoothManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var bluetoothPermissionGranted = false
    private var bluetoothEnabled = false

    companion object {
        private const val REQUEST_ENABLE_BLUETOOTH = 1
        private const val REQUEST_BLUETOOTH_PERMISSION = 2
    }

    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter!= null
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled?: false
    }

    fun requestEnableBluetooth() {
        if (!bluetoothEnabled) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            if (bluetoothAdapter == null) {
                // Bluetooth is not supported on this device
                // Handle this case accordingly, for example, show an error message
                return
            }
            if (!bluetoothAdapter.isEnabled) {
                // Bluetooth is not enabled on the device, request the user to enable it
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                (context as Activity).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
            } else {
                // Bluetooth is enabled, proceed with other Bluetooth operations
            }
        }
    }

    fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((context as Activity), arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_BLUETOOTH_PERMISSION)
            }
        } else {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((context as Activity), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_BLUETOOTH_PERMISSION)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ENABLE_BLUETOOTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothEnabled = true
                } else {
                    Toast.makeText(context, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bluetoothPermissionGranted = true
                    if (!bluetoothEnabled) {
                        requestEnableBluetooth()
                    }
                } else {
                    Toast.makeText(context, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}