package com.example.ta

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home) // Set the initial layout to home.xml

        setupHomeButtons() // Setup buttons in the home.xml layout
    }

    private fun setupHomeButtons() {
        val btnControl = findViewById<Button>(R.id.btn_control)
        btnControl.setOnClickListener {
            setContentView(R.layout.control) // Switch to control.xml layout
            setupControlButtons() // Setup buttons in the control.xml layout
        }

    }

    private fun setupControlButtons() {
        val btnHome = findViewById<Button>(R.id.btn_home)
        btnHome.setOnClickListener {
            setContentView(R.layout.home) // Switch to home.xml layout
            setupHomeButtons() // Setup buttons in the home.xml layout
        }

        val btnBack = findViewById<Button>(R.id.btn_back)
        btnBack.setOnClickListener {
            setContentView(R.layout.home) // Switch to home.xml layout
            setupHomeButtons() // Setup buttons in the home.xml layout
        }
    }
}
