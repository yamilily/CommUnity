package com.tmt.community

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.widget.TextView

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        val loginTextView = findViewById<TextView>(R.id.login_text_view)
        val promptText = getString(R.string.login_prompt)
        val actionText = getString(R.string.login_action)

        val spannableString = SpannableString("$promptText$actionText")
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            promptText.length,
            promptText.length + actionText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        loginTextView.text = spannableString

        loginTextView.setOnClickListener {
            finish()
        }

        val emailEditText = findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password_edit_text)
        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LOGIN_SUCCESS", "signInWithEmail:success")
                        navigateToMainActivity()
                    } else {
                        Log.w("LOGIN_FAILURE", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}