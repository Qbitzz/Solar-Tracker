package com.example.ta

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BluetoothManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothManager.adapter
    }
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    companion object {
        const val REQUEST_ENABLE_BLUETOOTH = 1
        const val REQUEST_BLUETOOTH_PERMISSION = 2
        private val TAG = BluetoothManager::class.java.simpleName
        private val UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled ?: false

    fun requestEnableBluetooth(activity: Activity) {
        if (!isBluetoothEnabled()) {
            bluetoothAdapter?.let {
                if (!it.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
                }
            } ?: run {
                Log.e(TAG, "Bluetooth is not supported on this device")
            }
        }
    }

    fun requestBluetoothPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_PERMISSION)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_BLUETOOTH_PERMISSION)
            }
        }
    }

    fun connectToDevice(device: BluetoothDevice): Boolean {
        if (checkPermissions()) {
            return try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SECURE)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                Log.d(TAG, "Connected to ${device.name}")
                true
            } catch (e: IOException) {
                Log.e(TAG, "Error connecting to device", e)
                false
            }
        } else {
            Log.e(TAG, "Bluetooth permissions not granted")
            return false
        }
    }

    fun sendMessage(message: String) {
        if (checkPermissions()) {
            try {
                outputStream?.write(message.toByteArray())
                outputStream?.flush()
                Log.d(TAG, "Sent message: $message")
            } catch (e: IOException) {
                Log.e(TAG, "Error sending message", e)
            }
        } else {
            Log.e(TAG, "Bluetooth permissions not granted")
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
            Log.d(TAG, "Disconnected from device")
        } catch (e: IOException) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
}
