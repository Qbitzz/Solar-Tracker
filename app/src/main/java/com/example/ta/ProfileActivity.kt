package com.example.ta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var profileUsername: TextView
    private lateinit var profileImage: ImageView
    private lateinit var buttonLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        profileUsername = findViewById(R.id.username)
        profileImage = findViewById(R.id.profileImage)
        buttonLogout = findViewById(R.id.buttonLogout)

        // Set the username
        profileUsername.text = auth.currentUser?.email ?: "Username"

        // Set logout button click listener
        buttonLogout.setOnClickListener {
            auth.signOut()
            showToast("Logged out successfully")
            switchToSignInLayout()
        }
    }

    private fun switchToSignInLayout() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
