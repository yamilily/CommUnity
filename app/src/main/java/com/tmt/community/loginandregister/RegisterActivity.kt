package com.tmt.community.loginandregister

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tmt.community.MainActivity
import com.tmt.community.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        database = Firebase.database("https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val emailEditText = findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password_edit_text)
        val phoneEditText = findViewById<TextInputEditText>(R.id.phone_edit_text)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginTextView = findViewById<TextView>(R.id.login_text_view)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Basic PH format check — must start with +63
            if (!phone.startsWith("+63") || phone.length != 13) {
                Toast.makeText(this, "Enter a valid PH number: +639XXXXXXXXX", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let {
                            saveUserToDatabase(it.uid, it.email, phone)
                        }
                    } else {
                        Toast.makeText(
                            baseContext,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

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
        loginTextView.setOnClickListener { finish() }
    }

    private fun saveUserToDatabase(userId: String, email: String?, phone: String) {
        val user = User(email = email, role = "resident", phoneNumber = phone)
        database.child("users").child(userId).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
