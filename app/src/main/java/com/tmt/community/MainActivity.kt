package com.tmt.community

// --- IMPORTS ---
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tmt.community.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_menu, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Handle the intent when the activity is created from a notification
        intent?.let { handleIntent(it) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle the intent when the activity is already running
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        // Use the modern, type-safe method to get the Parcelable extra
        val newAnnouncement: Announcement? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("new_announcement", Announcement::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("new_announcement")
        }

        // If an announcement was passed, navigate to the notifications fragment
        newAnnouncement?.let {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_notifications)
        }
    }
}