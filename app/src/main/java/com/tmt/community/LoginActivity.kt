package com.tmt.community

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is the only line we need for now. It connects this code
        // file to our beautiful XML layout.
        setContentView(R.layout.activity_login)
    }
}