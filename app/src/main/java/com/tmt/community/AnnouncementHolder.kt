package com.tmt.community

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.tmt.community.databinding.AnnouncementItemBinding
import java.util.concurrent.TimeUnit

class AnnouncementHolder(private val binding: AnnouncementItemBinding) : RecyclerView.ViewHolder(binding.root) {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    // Updated bind function to accept isAdmin status
    fun bind(announcement: Announcement, announcementRef: DatabaseReference, isAdmin: Boolean) {
        binding.announcementTitle.text = announcement.title
        binding.announcementMessage.text = announcement.message
        binding.announcementTimestamp.text = getFormattedTimestamp(announcement.timestamp)

        // --- DELETE LOGIC ---
        if (isAdmin) {
            binding.deleteButton.visibility = View.VISIBLE
            binding.deleteButton.setOnClickListener {
                showDeleteConfirmation(itemView.context, announcementRef)
            }
        } else {
            binding.deleteButton.visibility = View.GONE
        }

        // --- INTEREST LOGIC ---
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

    private fun showDeleteConfirmation(context: android.content.Context, ref: DatabaseReference) {
        AlertDialog.Builder(context)
            .setTitle("Delete Announcement")
            .setMessage("Are you sure you want to delete this announcement?")
            .setPositiveButton("Delete") { _, _ ->
                // Log that we are attempting to delete
                android.util.Log.d("DELETE_DEBUG", "Attempting to delete at: ${ref.toString()}")

                ref.removeValue().addOnSuccessListener {
                    Toast.makeText(context, "Announcement deleted", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    // --- THIS IS THE IMPORTANT PART ---
                    // If it fails, this will print the exact reason in your Logcat
                    android.util.Log.e("DELETE_DEBUG", "Delete failed: ${e.message}")
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateInterestUI(announcement: Announcement) {
        val interestedCount = announcement.interestedUsers.size
        binding.interestCountText.text = if (interestedCount == 1) "1 Interested" else "$interestedCount Interested"

        if (currentUserId != null && announcement.interestedUsers.containsKey(currentUserId)) {
            binding.interestButton.text = "I'm Interested"
            binding.interestButton.setIconResource(R.drawable.ic_star_filled)
            binding.interestButton.iconTint = ContextCompat.getColorStateList(itemView.context, R.color.colorPrimary)
        } else {
            binding.interestButton.text = "Interested"
            binding.interestButton.setIconResource(R.drawable.ic_star_outline)
            binding.interestButton.iconTint = ContextCompat.getColorStateList(itemView.context, R.color.colorOnSurface)
        }
    }

    private fun toggleInterest(announcementRef: DatabaseReference) {
        announcementRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val announcement = currentData.getValue(Announcement::class.java)
                    ?: return com.google.firebase.database.Transaction.success(currentData)

                if (announcement.interestedUsers.containsKey(currentUserId)) {
                    announcement.interestedUsers.remove(currentUserId)
                } else {
                    if (currentUserId != null) {
                        announcement.interestedUsers[currentUserId] = true
                    }
                }
                currentData.value = announcement
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(error: com.google.firebase.database.DatabaseError?, committed: Boolean, currentData: com.google.firebase.database.DataSnapshot?) {}
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