package com.example.ta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        // Setup buttons in the control.xml layout
        setupControlButtons()
    }

    private fun setupControlButtons() {
        val btnHome = findViewById<Button>(R.id.btn_home)
        btnHome.setOnClickListener {
            switchToHomeLayout()
        }

        val btnBack = findViewById<Button>(R.id.btn_back)
        btnBack.setOnClickListener {
            switchToHomeLayout()
        }

        // Find the LinearLayout with id atas
        val atas = findViewById<LinearLayout>(R.id.atas)

        // Set a click listener for the LinearLayout
        atas.setOnClickListener {
            Toast.makeText(this, "LinearLayout Atas Clicked", Toast.LENGTH_SHORT).show()
        }

        // Find the LinearLayout with id kiri
        val kiri = findViewById<LinearLayout>(R.id.kiri)

        // Set a click listener for the LinearLayout
        kiri.setOnClickListener {
            Toast.makeText(this, "LinearLayout Kiri Clicked", Toast.LENGTH_SHORT).show()
        }

        // Find the LinearLayout with id kanan
        val kanan = findViewById<LinearLayout>(R.id.kanan)

        // Set a click listener for the LinearLayout
        kanan.setOnClickListener {
            Toast.makeText(this, "LinearLayout Kanan Clicked", Toast.LENGTH_SHORT).show()
        }

        // Find the LinearLayout with id bawah
        val bawah = findViewById<LinearLayout>(R.id.bawah)

        // Set a click listener for the LinearLayout
        bawah.setOnClickListener {
            Toast.makeText(this, "LinearLayout Bawah Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchToHomeLayout() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    fun onLinearLayoutClick(view: View) {
        when (view.id) {
            R.id.atas -> {
                Toast.makeText(this, "LinearLayout Atas Clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.kiri -> {
                Toast.makeText(this, "LinearLayout Kiri Clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.kanan -> {
                Toast.makeText(this, "LinearLayout Kanan Clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.bawah -> {
                Toast.makeText(this, "LinearLayout Bawah Clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }
}