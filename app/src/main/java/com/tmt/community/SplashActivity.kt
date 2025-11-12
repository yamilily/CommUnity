package com.tmt.community

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // REMOVED: splashScreen.setKeepOnScreenCondition { true }
        // That line was the entire cause of the deadlock.

        // --- THE REST OF THE CODE IS CORRECT ---
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val iconView = splashScreenView.iconView
            val iconParent = iconView.parent as ViewGroup

            val lottieView = LottieAnimationView(this).apply {
                setAnimation(R.raw.comm_unity_logo_animation)
                repeatMode = LottieDrawable.RESTART
                repeatCount = 0
            }

            val scaleFactor = 1.6f
            lottieView.scaleX = scaleFactor
            lottieView.scaleY = scaleFactor
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            lottieView.translationX = screenWidth * 0.35f
            lottieView.translationY = screenHeight * 0.14f

            val lottieParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            iconParent.addView(lottieView, lottieParams)
            iconParent.removeView(iconView)

            lottieView.playAnimation()

            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view, View.ALPHA, 1f, 0f
            ).apply {
                duration = 300L
                doOnEnd { splashScreenView.remove() }
            }

            lottieView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    fadeOut.start()
                    routeToAppropriateActivity()
                }
            })
        }
    }

    private fun routeToAppropriateActivity() {
        val auth = Firebase.auth
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}