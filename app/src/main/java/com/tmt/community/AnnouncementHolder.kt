package com.tmt.community

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
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

    fun bind(announcement: Announcement, announcementRef: DatabaseReference, isAdmin: Boolean) {
        binding.announcementTitle.text = announcement.title
        binding.announcementMessage.text = announcement.message
        binding.announcementTimestamp.text = getFormattedTimestamp(announcement.timestamp)

        // --- ADMIN CONTROLS (EDIT & DELETE) ---
        if (isAdmin) {
            binding.deleteButton.visibility = View.VISIBLE
            binding.editButton.visibility = View.VISIBLE

            binding.deleteButton.setOnClickListener {
                showDeleteConfirmation(itemView.context, announcementRef)
            }

            binding.editButton.setOnClickListener {
                showEditDialog(itemView.context, announcement, announcementRef)
            }
        } else {
            binding.deleteButton.visibility = View.GONE
            binding.editButton.visibility = View.GONE
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

    private fun showEditDialog(context: Context, announcement: Announcement, ref: DatabaseReference) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit Announcement")

        // Create a layout for the inputs programmatically
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        // Title Input
        val titleInput = EditText(context)
        titleInput.hint = "Title"
        titleInput.setText(announcement.title)
        layout.addView(titleInput)

        // Message Input
        val messageInput = EditText(context)
        messageInput.hint = "Message"
        messageInput.setText(announcement.message)
        messageInput.minLines = 3
        messageInput.setPadding(0, 30, 0, 0) // Add some spacing
        layout.addView(messageInput)

        builder.setView(layout)

        // Buttons
        builder.setPositiveButton("Save") { _, _ ->
            val newTitle = titleInput.text.toString().trim()
            val newMessage = messageInput.text.toString().trim()

            if (newTitle.isNotEmpty() && newMessage.isNotEmpty()) {
                // Update only the title and message fields
                val updates = mapOf<String, Any>(
                    "title" to newTitle,
                    "message" to newMessage
                )

                ref.updateChildren(updates).addOnSuccessListener {
                    Toast.makeText(context, "Announcement updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showDeleteConfirmation(context: Context, ref: DatabaseReference) {
        AlertDialog.Builder(context)
            .setTitle("Delete Announcement")
            .setMessage("Are you sure you want to delete this announcement? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                ref.removeValue().addOnSuccessListener {
                    Toast.makeText(context, "Announcement deleted", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
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