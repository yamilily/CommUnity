package com.tmt.community

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object AnnouncementHolder {
    // The LiveData now holds our new Announcement object
    val newAnnouncement = MutableLiveData<Announcement?>()

    init {
        val database = Firebase.database("https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("announcements")

        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Convert the entire snapshot into an Announcement object
                val announcement = snapshot.getValue(Announcement::class.java)
                newAnnouncement.postValue(announcement)
            }
            // ... other functions are unchanged
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}