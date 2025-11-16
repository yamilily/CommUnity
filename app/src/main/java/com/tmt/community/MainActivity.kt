package com.tmt.community

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.tmt.community.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted.
        } else {
            // Permission is denied.
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // This line correctly sets up the NavController to handle clicks.
        // We must NOT override this with a different listener.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_notifications, R.id.navigation_menu
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        askNotificationPermission()
        Firebase.messaging.subscribeToTopic("announcements") // Added this line just in case for our test later

        // --- CORRECTED IN-APP NOTIFICATION LOGIC ---

        AnnouncementHolder.newAnnouncement.observe(this) { announcement ->
            if (announcement != null) {
                val currentDestinationId = navController.currentDestination?.id
                // Use the CORRECT ID
                if (currentDestinationId != R.id.navigation_notifications) {
                    val badge = navView.getOrCreateBadge(R.id.navigation_notifications) // Use the CORRECT ID
                    badge.isVisible = true
                }
            }
        }

        // Use a separate listener that DOES NOT interfere with navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_notifications) {
                // When we successfully navigate to the notifications tab, remove the badge.
                navView.removeBadge(R.id.navigation_notifications) // Use the CORRECT ID
            }
        }
    }
}