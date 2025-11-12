package com.tmt.community

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // ... (your existing animation code is here)
            val iconView = splashScreenView.iconView
            val iconParent = iconView.parent as ViewGroup

            val lottieView = LottieAnimationView(this).apply {
                setAnimation(R.raw.comm_unity_logo_animation)
                repeatMode = LottieDrawable.RESTART
                repeatCount = 0
            }
            val scaleFactor = 1f
            lottieView.scaleX = scaleFactor
            lottieView.scaleY = scaleFactor
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            lottieView.translationX = screenWidth * 0f
            lottieView.translationY = screenHeight * 0f
            val lottieParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            iconParent.addView(lottieView, lottieParams)
            iconParent.removeView(iconView)

            lottieView.playAnimation()

            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view, View.ALPHA, 0f, 0f
            ).apply {
                duration = 300L
                doOnEnd { splashScreenView.remove() }
            }

            lottieView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}

                // --- THIS IS THE NEW LOGIC ---
                override fun onAnimationEnd(animation: Animator) {
                    // When the animation finishes, check if a user is logged in.
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        // If logged in, go straight to MainActivity.
                        navigateToMainActivity()
                    } else {
                        // If not logged in, just start the fade out and let the login screen appear.
                        fadeOut.start()
                    }
                }
            })
        }

        // --- Login logic remains the same ---
        val emailEditText = findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password_edit_text)
        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            // ... (your existing login button code is here, no changes needed)
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