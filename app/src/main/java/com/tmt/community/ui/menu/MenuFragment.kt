package com.tmt.community.ui.menu

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tmt.community.databinding.FragmentMenuBinding
import com.tmt.community.loginandregister.LoginActivity

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val databaseUrl = "https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users").child(currentUser.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.child("role").getValue(String::class.java) ?: "resident"

                    // Display User Info
                    // NOTE: This references the ID 'text_menu' from the XML
                    binding.textMenu.text = "Logged in as:\n${currentUser.email}"

                    if (role == "admin") {
                        // Show admin controls
                        binding.manageUserButton.visibility = View.VISIBLE
                        binding.textMenu.append("\n(Admin)")
                    } else {
                        // Hide admin controls
                        binding.manageUserButton.visibility = View.GONE
                        binding.textMenu.append("\n(Resident)")
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        // Handle Manage User Button
        binding.manageUserButton.setOnClickListener {
            showRoleManagementDialog()
        }

        // Handle Logout
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return root
    }

    private fun showRoleManagementDialog() {
        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Manage User Role")
        builder.setMessage("Enter the resident's email and select an action:")

        // Set up the input
        val input = EditText(context)
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = "user@example.com"

        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 0, 50, 0)
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        // PROMOTE BUTTON
        builder.setPositiveButton("Promote to Admin") { _, _ ->
            val email = input.text.toString().trim()
            if (validateEmail(email)) {
                updateUserRole(email, "admin")
            }
        }

        // DEMOTE BUTTON
        builder.setNeutralButton("Demote to Resident") { _, _ ->
            val email = input.text.toString().trim()
            if (validateEmail(email)) {
                updateUserRole(email, "resident")
            }
        }

        // CANCEL BUTTON
        builder.setNegativeButton("Cancel", null)

        builder.show()
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        // Safety check: Don't allow changing your own role!
        if (email == auth.currentUser?.email) {
            Toast.makeText(context, "You cannot change your own role.", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun updateUserRole(email: String, newRole: String) {
        val usersRef = FirebaseDatabase.getInstance(databaseUrl).getReference("users")

        // Search for user by email
        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val userRef = userSnapshot.ref

                            // Check if they already have this role
                            val currentRole = userSnapshot.child("role").getValue(String::class.java)
                            if (currentRole == newRole) {
                                Toast.makeText(context, "User is already a $newRole.", Toast.LENGTH_SHORT).show()
                                return
                            }

                            // Perform the update
                            userRef.child("role").setValue(newRole)
                                .addOnSuccessListener {
                                    val action = if (newRole == "admin") "promoted to Admin" else "demoted to Resident"
                                    Toast.makeText(context, "Success! $email was $action.", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to update role.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(context, "User not found with that email.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}