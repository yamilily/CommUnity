package com.tmt.community

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.LottieAnimationView

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()

        setContentView(R.layout.activity_login)
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val lottieView = LottieAnimationView(baseContext).apply {
                setAnimation(R.raw.app_logo_animation)
                scaleType = ImageView.ScaleType.FIT_CENTER
                repeatCount = 0
                playAnimation()
            }

            val splashScreenLayout = splashScreenView.view as ViewGroup

            splashScreenLayout.addView(lottieView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)

            val alphaOut = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            )
            alphaOut.duration = 400L
            alphaOut.doOnEnd {
                splashScreenView.remove()
            }

            alphaOut.start()
        }
    }
}