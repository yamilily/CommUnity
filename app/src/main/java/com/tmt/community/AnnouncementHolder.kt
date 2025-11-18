package com.tmt.community

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tmt.community.databinding.AnnouncementItemBinding
import java.util.concurrent.TimeUnit

class AnnouncementHolder(private val binding: AnnouncementItemBinding) : RecyclerView.ViewHolder(binding.root) {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    fun bind(announcement: Announcement, announcementRef: DatabaseReference) {
        binding.announcementTitle.text = announcement.title
        binding.announcementMessage.text = announcement.message

        // Set a readable timestamp
        binding.announcementTimestamp.text = getFormattedTimestamp(announcement.timestamp)

        // Handle visibility of the interest feature
        if (announcement.showInterestButton) {
            binding.interestGroup.visibility = View.VISIBLE

            updateInterestUI(announcement)

            binding.interestButton.setOnClickListener {
                if (currentUserId != null) {
                    toggleInterest(announcementRef)
                }
            }
        } else {
            binding.interestGroup.visibility = View.GONE
        }
    }

    private fun updateInterestUI(announcement: Announcement) {
        val interestedCount = announcement.interestedUsers.size
        binding.interestCountText.text = if (interestedCount == 1) "1 Interested" else "$interestedCount Interested"

        if (currentUserId != null && announcement.interestedUsers.containsKey(currentUserId)) {
            // User IS interested
            binding.interestButton.text = "I'm Interested"
            binding.interestButton.setIconResource(R.drawable.ic_star_filled)
            // CORRECTED: Use the color from your colors.xml
            binding.interestButton.iconTint = ContextCompat.getColorStateList(itemView.context, R.color.colorPrimary)
        } else {
            // User is NOT interested
            binding.interestButton.text = "Interested"
            binding.interestButton.setIconResource(R.drawable.ic_star_outline)
            // CORRECTED: Use the color from your colors.xml
            binding.interestButton.iconTint = ContextCompat.getColorStateList(itemView.context, R.color.colorOnSurface)
        }
    }


    private fun toggleInterest(announcementRef: DatabaseReference) {
        announcementRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val announcement = currentData.getValue(Announcement::class.java)
                    ?: return com.google.firebase.database.Transaction.success(currentData)

                if (announcement.interestedUsers.containsKey(currentUserId)) {
                    // User is already interested, so remove them
                    announcement.interestedUsers.remove(currentUserId)
                } else {
                    // User is not interested, so add them
                    if (currentUserId != null) {
                        announcement.interestedUsers[currentUserId] = true
                    }
                }
                currentData.value = announcement
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                currentData: com.google.firebase.database.DataSnapshot?
            ) {
                // Transaction completed
            }
        })
    }

    private fun getFormattedTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        if (seconds < 60) return "Just now"

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (minutes < 60) return "${minutes}m ago"

        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        if (hours < 24) return "${hours}h ago"

        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return "${days}d ago"
    }
}