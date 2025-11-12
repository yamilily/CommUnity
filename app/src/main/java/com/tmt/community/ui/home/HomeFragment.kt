package com.tmt.community.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Important import for the cleaner ViewModel syntax
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tmt.community.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    // This is the modern, null-safe way to handle view binding in Fragments
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // This is the clean Kotlin way to get the ViewModel
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonSend.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            val body = binding.editTextBody.text.toString().trim()

            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                sendAnnouncement(title, body)
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the userRole LiveData from the ViewModel
        homeViewModel.userRole.observe(viewLifecycleOwner) { role ->
            // This code runs whenever the role is fetched from Firebase
            if (role == "admin") {
                // If user is an admin, SHOW the panel
                binding.adminPanelCard.visibility = View.VISIBLE
            } else {
                // If user is a resident (or anything else), HIDE the panel
                binding.adminPanelCard.visibility = View.GONE
                // You could add another view for residents and show it here!
            }
        }
    }

    private fun sendAnnouncement(title: String, body: String) {
        val database = Firebase.database.reference.child("announcements")
        val announcementId = database.push().key ?: return // Exit if key is null

        val announcement = mapOf(
            "title" to title,
            "body" to body,
            "timestamp" to System.currentTimeMillis()
        )

        database.child(announcementId).setValue(announcement)
            .addOnSuccessListener {
                Toast.makeText(context, "Announcement sent!", Toast.LENGTH_SHORT).show()
                binding.editTextTitle.text?.clear()
                binding.editTextBody.text?.clear()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to send announcement.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // This is crucial to prevent memory leaks
        _binding = null
    }
}