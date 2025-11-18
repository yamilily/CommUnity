package com.tmt.community.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.tmt.community.Announcement
import com.tmt.community.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val database = FirebaseDatabase.getInstance("https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Announcements")

        // ... inside HomeFragment.kt

        binding.postButton.setOnClickListener {
            val title = binding.titleEditText.text.toString().trim()
            val message = binding.messageEditText.text.toString().trim()
            val showInterest = binding.interestSwitch.isChecked

            if (title.isNotEmpty() && message.isNotEmpty()) {
                val announcementId = database.push().key!!

                // CORRECTED: Create an empty object, then apply properties.
                // This works with the required no-argument constructor.
                val announcement = Announcement().apply {
                    this.title = title
                    this.message = message
                    this.timestamp = System.currentTimeMillis()
                    this.showInterestButton = showInterest
                }

                database.child(announcementId).setValue(announcement).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Announcement posted", Toast.LENGTH_SHORT).show()
                        binding.titleEditText.text?.clear()
                        binding.messageEditText.text?.clear()
                        binding.interestSwitch.isChecked = false
                    } else {
                        Toast.makeText(context, "Failed to post", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

// ... rest of the file
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}