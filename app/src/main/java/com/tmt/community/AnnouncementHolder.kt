package com.tmt.community

// --- IMPORTS ---
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.tmt.community.databinding.AnnouncementItemBinding

class AnnouncementHolder(private val binding: AnnouncementItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(announcement: Announcement) {
        binding.announcementTitle.text = announcement.title
        binding.announcementMessage.text = announcement.message // Corrected this ID
        binding.announcementDate.text = announcement.date

        if (announcement.showInterestButton) {
            binding.interestButton.visibility = View.VISIBLE
            binding.interestButton.setOnClickListener {
                Toast.makeText(binding.root.context, "You've shown interest!", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.interestButton.visibility = View.GONE
        }
    }
}