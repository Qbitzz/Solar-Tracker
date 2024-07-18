package com.example.ta

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DeviceListActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceList: ListView
    private lateinit var btnConnectDevice: Button
    private var selectedDevice: BluetoothDevice? = null
    private lateinit var bluetoothManager: BluetoothManager

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        bluetoothManager = BluetoothManager(this)

        deviceList = findViewById(R.id.device_list)
        btnConnectDevice = findViewById(R.id.btn_connect_device)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        populateDeviceList()

        btnConnectDevice.setOnClickListener {
            selectedDevice?.let { device ->
                val intent = Intent()
                intent.putExtra("device_address", device.address)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } ?: run {
                Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show()
            }
        }

        val btnBack = findViewById<Button>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        deviceList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            selectedDevice = bluetoothAdapter.bondedDevices.elementAt(position)
            Toast.makeText(this, "Selected: ${selectedDevice?.name}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun populateDeviceList() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        val list: ArrayList<String> = ArrayList()

        pairedDevices?.forEach { device ->
            list.add(device.name + "\n" + device.address)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        deviceList.adapter = adapter
    }
}