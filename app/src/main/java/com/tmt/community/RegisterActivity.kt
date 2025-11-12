package com.tmt.community

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
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginTextView = findViewById<TextView>(R.id.login_text_view)

        // --- REGISTER BUTTON LOGIC (Unchanged) ---
        registerButton.setOnClickListener {
            // ... (your existing registration code)
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let {
                            saveUserToDatabase(it.uid, it.email)
                        }
                    } else {
                        Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // --- CORRECTED TEXT AND CLICK LISTENER FOR "LOGIN" PROMPT ---
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
            // Go back to the LoginActivity
            finish()
        }
    }

    private fun saveUserToDatabase(userId: String, email: String?) {
        // ... (your existing saveUserToDatabase code is here)
        val user = User(email = email, role = "resident")
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