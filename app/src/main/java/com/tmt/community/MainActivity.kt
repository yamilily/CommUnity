package com.tmt.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Databasee
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app/")

        // Set the custom toolbar as the action bar
        setSupportActionBar(binding.toolbar)

        // Check if a user is logged in. If not, redirect to LoginActivity
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return // Stop execution of onCreate
        }

        // Fetch the user's role and set up the UI accordingly
        checkUserRoleAndSetupUI()
    }

    private fun checkUserRoleAndSetupUI() {
        val firebaseUser = auth.currentUser
        // If for some reason user is null, exit to login
        if (firebaseUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userRef = db.reference.child("users").child(firebaseUser.uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the role, default to "resident" if not found
                val role = snapshot.child("role").getValue(String::class.java) ?: "resident"

                // Now that we have the role, setup the navigation
                setupNavigation(role)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to read user role.", error.toException())
                // On error, default to resident view for security
                setupNavigation("resident")
            }
        })
    }

    private fun setupNavigation(role: String) {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Show/Hide the Admin Panel (Home Fragment) based on role
        val homeMenuItem = navView.menu.findItem(R.id.navigation_home)

        val appBarConfiguration: AppBarConfiguration

        if (role == "admin") {
            // ADMINS: Show the home button and use the default navigation graph
            homeMenuItem.isVisible = true
            appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_home, R.id.navigation_menu, R.id.navigation_notifications)
            )
        } else {
            // RESIDENTS: Hide the home button
            homeMenuItem.isVisible = false

            // IMPORTANT: We must change the start destination for residents
            // because the default start destination (navigation_home) is now hidden.
            val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
            navGraph.setStartDestination(R.id.navigation_notifications) // Set announcements as the default screen
            navController.graph = navGraph

            appBarConfiguration = AppBarConfiguration(
                // Home is removed from the top-level destinations
                setOf(R.id.navigation_menu, R.id.navigation_notifications)
            )
        }

        // Connect the NavController to the ActionBar and BottomNavigationView
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // This is for handling notifications, leave it as is
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // ... (your existing handleIntent code)
    }
}