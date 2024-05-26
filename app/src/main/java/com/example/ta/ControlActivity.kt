package com.example.ta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ControlActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        // Setup buttons in the control.xml layout
        setupControlButtons()
    }

    private fun setupControlButtons() {
        findViewById<Button>(R.id.btn_home).setOnClickListener(this)
        findViewById<Button>(R.id.btn_back).setOnClickListener(this)

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
            R.id.btn_home, R.id.btn_back -> switchToHomeLayout()
            R.id.atas -> showToast("LinearLayout Atas Clicked")
            R.id.kiri -> showToast("ImageView Kiri Clicked")
            R.id.kanan -> showToast("ImageView Kanan Clicked")
            R.id.bawah -> showToast("LinearLayout Bawah Clicked")
        }
    }

    private fun switchToHomeLayout() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}