package com.tmt.community

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.tmt.community.databinding.ActivityMainBinding
import com.tmt.community.loginandregister.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    // --- MOVED THE LAUNCHER HERE, AT THE TOP LEVEL OF THE CLASS ---
    // This handles the result of the permission request. It must be initialized
    // before onCreate() is called.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. You can expect notifications to appear.
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // Permission is denied. Notifications will be blocked.
            Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app/")

        setSupportActionBar(binding.toolbar)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Now this function call will work correctly
        askNotificationPermission()

        checkUserRoleAndSetupUI()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API 33+ (Android 13 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // If permission is not granted, launch the request.
                // The result is handled by the launcher we defined above.
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
                setupNavigation("resident")
            }
        })
    }

    private fun setupNavigation(role: String) {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val homeMenuItem = navView.menu.findItem(R.id.navigation_home)
        val appBarConfiguration: AppBarConfiguration

        if (role == "admin") {
            homeMenuItem.isVisible = true
            appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_home, R.id.navigation_menu, R.id.navigation_notifications)
            )
        } else {
            homeMenuItem.isVisible = false
            val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
            navGraph.setStartDestination(R.id.navigation_notifications)
            navController.graph = navGraph
            appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_menu, R.id.navigation_notifications)
            )
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    // --- ADDED LOGIC TO USE THE 'intent' PARAMETER ---
    private fun handleIntent(intent: Intent?) {
        // This function is called when the activity is launched from a notification tap
        intent?.extras?.let {
            val title = it.getString("title")
            val message = it.getString("message")
            Log.d("NotificationTap", "Activity opened from notification. Title: $title, Message: $message")
        }
    }
}