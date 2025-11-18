package com.tmt.community.ui.notifications

// --- IMPORTS ---
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tmt.community.Announcement

class NotificationsViewModel : ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("Announcements")
    private val _announcements = MutableLiveData<List<Announcement>>()
    val announcements: LiveData<List<Announcement>> = _announcements

    init {
        fetchAnnouncements()
    }

    private fun fetchAnnouncements() {
        databaseReference.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcementList = mutableListOf<Announcement>()
                for (announcementSnapshot in snapshot.children) {
                    val announcement = announcementSnapshot.getValue(Announcement::class.java)
                    announcement?.let { announcementList.add(it) }
                }
                announcementList.reverse()
                _announcements.value = announcementList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}