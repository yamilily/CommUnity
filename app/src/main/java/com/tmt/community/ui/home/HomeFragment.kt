package com.tmt.community.ui.home

// --- IMPORTS ---
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tmt.community.Announcement
import com.tmt.community.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    // Add this line
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize databaseReference
        databaseReference = FirebaseDatabase.getInstance().getReference("Announcements")

        binding.sendAnnouncementButton.setOnClickListener {
            sendAnnouncement()
        }
        return root
    }

    // This is the corrected function
    private fun sendAnnouncement() {
        val title = binding.announcementTitle.text.toString().trim()
        val message = binding.announcementBody.text.toString().trim()
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
                    binding.announcementTitle.text.clear()
                    binding.announcementBody.text.clear()
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