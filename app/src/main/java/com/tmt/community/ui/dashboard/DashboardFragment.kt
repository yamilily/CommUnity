package com.tmt.community.ui.dashboard // Make sure this matches your package!

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tmt.community.AnnouncementHolder
import com.tmt.community.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // This is the magic part! We are now "listening" to our radio station.
        AnnouncementHolder.newAnnouncement.observe(viewLifecycleOwner) { newText ->
            // This block of code will run EVERY time a new announcement is received.
            if (newText != null) {
                // Call our function to add the new "box" to the screen.
                addAnnouncementView(newText)
            }
        }
        return root
    }

    // This is our new function to create and add the "rectangular box thingy"
    private fun addAnnouncementView(text: String) {
        // Find our container from the XML layout
        val container = binding.announcementsContainer

        // Create a new TextView programmatically
        val announcementTextView = TextView(requireContext()).apply {
            // Set the text from the notification
            this.text = text
            textSize = 16f
            // Set some padding and margins to make it look nice
            setPadding(24, 24, 24, 24)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 0, 0)
            this.layoutParams = layoutParams
            // Set a background to make it look like a box
            setBackgroundResource(android.R.drawable.editbox_background) // A simple default background
        }

        // Add the new TextView to our container at the top (position 1, after the title)
        container.addView(announcementTextView, 1)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}