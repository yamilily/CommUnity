package com.tmt.community

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.tmt.community.databinding.ActivityMainBinding
import com.tmt.community.loginandregister.LoginActivity

// Removed unnecessary permission-related imports that are now handled
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    // The broadcast receiver is still useful for instant foreground updates
    private val newAnnouncementReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkAndShowBadge()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialization
        auth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.toolbar)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
        setupNavigationAndUserRole()
    }

    override fun onResume() {
        super.onResume()
        // Register the receiver for foreground updates
        LocalBroadcastManager.getInstance(this).registerReceiver(
            newAnnouncementReceiver,
            IntentFilter("new-announcement-event")
        )
        // CRUCIAL: Always check the badge state when the app becomes visible
        checkAndShowBadge()
    }

    override fun onPause() {
        super.onPause()
        // Unregister the receiver to prevent memory leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newAnnouncementReceiver)
    }

    private fun checkAndShowBadge() {
        val prefs = getSharedPreferences("CommUnityPrefs", Context.MODE_PRIVATE)
        val hasNewAnnouncement = prefs.getBoolean("new_announcement_badge", false)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        if (hasNewAnnouncement && navController.currentDestination?.id != R.id.navigation_notifications) {
            val badge = binding.navView.getOrCreateBadge(R.id.navigation_notifications)
            badge.isVisible = true
        }
    }

    private fun removeNotificationBadge() {
        // Remove the visual badge
        binding.navView.removeBadge(R.id.navigation_notifications)

        // CRUCIAL: Clear the flag in SharedPreferences
        val prefs = getSharedPreferences("CommUnityPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("new_announcement_badge", false).apply()
    }

    // Simplified setup function
    private fun setupNavigationAndUserRole() {
        val db = FirebaseDatabase.getInstance("https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val userRef = db.reference.child("users").child(auth.currentUser!!.uid)

        userRef.get().addOnSuccessListener { snapshot ->
            val role = snapshot.child("role").getValue(String::class.java) ?: "resident"

            val navView: BottomNavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)

            navView.menu.findItem(R.id.navigation_home).isVisible = role == "admin"

            val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_home, R.id.navigation_menu, R.id.navigation_notifications)
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            // Set start destination for residents
            if (role != "admin") {
                val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
                navGraph.setStartDestination(R.id.navigation_notifications)
                navController.graph = navGraph
            }

            // The listener to remove the badge when the user navigates to the right tab
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.navigation_notifications) {
                    removeNotificationBadge()
                }
            }
        }.addOnFailureListener {
            Log.e("MainActivity", "Failed to get user role", it)
        }
    }
}