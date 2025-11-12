package com.tmt.community

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This activity has no layout. It just decides where to go.

        val auth = Firebase.auth
        if (auth.currentUser != null) {
            // User is already logged in, go straight to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // User is not logged in, go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Finish this activity so the user can't press "back" to get to it.
        finish()
    }
}