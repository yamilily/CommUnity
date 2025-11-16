package com.tmt.community.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tmt.community.Announcement
import com.tmt.community.AnnouncementHolder
import com.tmt.community.R
import com.tmt.community.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe the new Announcement object
        AnnouncementHolder.newAnnouncement.observe(viewLifecycleOwner) { announcement ->
            if (announcement != null) {
                addAnnouncementView(announcement)
            }
        }
        return root
    }

    // This function now takes an Announcement object and inflates our custom layout
    private fun addAnnouncementView(announcement: Announcement) {
        val container = binding.announcementsContainer
        val inflater = LayoutInflater.from(requireContext())

        // Inflate our new custom layout
        val announcementView = inflater.inflate(R.layout.announcement_item, container, false)

        // Find the TextViews inside our new layout
        val titleTextView = announcementView.findViewById<TextView>(R.id.announcement_title)
        val bodyTextView = announcementView.findViewById<TextView>(R.id.announcement_body)

        // Set the text from the Announcement object
        titleTextView.text = announcement.title
        bodyTextView.text = announcement.body

        // Add the new card view to the top of the list
        container.addView(announcementView, 0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}