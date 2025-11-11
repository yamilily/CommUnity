package com.tmt.community

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        setContentView(R.layout.activity_login)

        splashScreen.setOnExitAnimationListener { splashScreenView ->

            val iconView = splashScreenView.iconView
            val iconParent = iconView.parent as ViewGroup

            val lottieView = LottieAnimationView(this).apply {
                setAnimation(R.raw.app_logo_animation)
                repeatMode = LottieDrawable.RESTART
                repeatCount = 0 // Play only once
            }

            // --- HACKY FIX STARTS HERE ---

            // 1. SCALE UP to hide the watermark
            // We make the view 20% larger than the container. This pushes the
            // watermark (in the bottom-right) off the screen.
            // You can adjust this value (e.g., 1.15f or 1.25f) if needed.
            val scaleFactor = 1.2f
            lottieView.scaleX = scaleFactor
            lottieView.scaleY = scaleFactor

            // 2. REPOSITION to re-center the logo
            // Because the logo is in the top-left of its 600x600 canvas, and we just
            // scaled the whole view, we need to manually pull it back toward the center.
            // We'll move it right by 10% of the screen width and down by 10% of the screen height.
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // Adjust these values if the centering is slightly off.
            lottieView.translationX = screenWidth * 0.25f
            lottieView.translationY = screenHeight * 0.11f

            // --- HACKY FIX ENDS HERE ---

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
                }
            })
        }
    }
}