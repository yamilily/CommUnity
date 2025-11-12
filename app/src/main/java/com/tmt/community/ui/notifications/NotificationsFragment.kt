package com.tmt.community.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tmt.community.AnnouncementHolder
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

        AnnouncementHolder.newAnnouncement.observe(viewLifecycleOwner) { newText ->
            if (newText != null) {
                addAnnouncementView(newText)
            }
        }
        return root
    }

    private fun addAnnouncementView(text: String) {
        val container = binding.announcementsContainer

        val announcementTextView = TextView(requireContext()).apply {
            this.text = text
            textSize = 16f
            setPadding(24, 24, 24, 24)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 16, 0, 0)
            this.layoutParams = layoutParams
            setBackgroundResource(android.R.drawable.editbox_background)
        }

        container.addView(announcementTextView, 1)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}