package com.tmt.community.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tmt.community.Announcement
import com.tmt.community.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        databaseReference = FirebaseDatabase.getInstance().getReference("Announcements")

        // Use the CORRECT ID: post_announcement_button -> postAnnouncementButton
        binding.postAnnouncementButton.setOnClickListener {
            sendAnnouncement()
        }
        return binding.root
    }

    private fun sendAnnouncement() {
        // Use the CORRECT IDs: announcement_title_input -> announcementTitleInput
        // and announcement_message_input -> announcementMessageInput
        val title = binding.announcementTitleInput.text.toString().trim()
        val message = binding.announcementMessageInput.text.toString().trim()
        val showInterest = binding.interestButtonCheckbox.isChecked

        if (title.isNotEmpty() && message.isNotEmpty()) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val announcement = Announcement(
                title = title,
                message = message,
                date = date,
                timestamp = System.currentTimeMillis(),
                showInterestButton = showInterest
            )

            databaseReference.push().setValue(announcement).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Announcement posted", Toast.LENGTH_SHORT).show()
                    binding.announcementTitleInput.text.clear()
                    binding.announcementMessageInput.text.clear()
                    binding.interestButtonCheckbox.isChecked = false
                } else {
                    Toast.makeText(context, "Failed to post announcement", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}