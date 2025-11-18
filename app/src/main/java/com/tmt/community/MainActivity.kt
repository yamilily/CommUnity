package com.tmt.community

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.tmt.community.databinding.ActivityMainBinding
import com.tmt.community.loginandregister.LoginActivity

class MainActivity : AppCompatActivity() {

    // --- CLASS-LEVEL PROPERTIES ---
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private val newAnnouncementReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            if (navController.currentDestination?.id != R.id.navigation_notifications) {
                showNotificationBadge()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ACTIVITY LIFECYCLE METHODS ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialization
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app/")
        setSupportActionBar(binding.toolbar)

        // Check login status
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Subscribe to FCM Topic
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) "Subscribed to announcements!" else "Subscription failed."
                Log.d("FCM_TOPIC", msg)
            }

        // Run setup functions
        askNotificationPermission()
        checkUserRoleAndSetupUI()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            newAnnouncementReceiver,
            IntentFilter("new-announcement-event")
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newAnnouncementReceiver)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    // --- PERMISSION AND UI SETUP FUNCTIONS ---
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkUserRoleAndSetupUI() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userRef = db.reference.child("users").child(firebaseUser.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val role = snapshot.child("role").getValue(String::class.java) ?: "resident"
                setupNavigation(role)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to read user role.", error.toException())
                setupNavigation("resident") // Default to resident on error for security
            }
        })
    }

    private fun setupNavigation(role: String) {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Show/hide admin tab
        navView.menu.findItem(R.id.navigation_home).isVisible = role == "admin"

        // Configure AppBar
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_menu, R.id.navigation_notifications)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up BottomNav with NavController
        navView.setupWithNavController(navController)

        // Add a listener to remove the badge when user navigates to the notifications tab
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_notifications) {
                removeNotificationBadge()
            }
        }

        // Set start destination for residents if default is hidden
        if (role != "admin") {
            val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
            navGraph.setStartDestination(R.id.navigation_notifications)
            navController.graph = navGraph
        }
    }

    // --- BADGE AND INTENT HELPER FUNCTIONS ---
    private fun showNotificationBadge() {
        val badge = binding.navView.getOrCreateBadge(R.id.navigation_notifications)
        badge.isVisible = true
    }

    private fun removeNotificationBadge() {
        binding.navView.removeBadge(R.id.navigation_notifications)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.extras?.let {
            val title = it.getString("title")
            val message = it.getString("message")
            Log.d("NotificationTap", "Activity opened from notification. Title: $title, Message: $message")
        }
    }
}