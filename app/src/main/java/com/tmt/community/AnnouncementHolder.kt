package com.tmt.community // Make sure this matches your package name!

import androidx.lifecycle.MutableLiveData

// This object holds our announcement data so the whole app can access it.
// The `val newAnnouncement` is our "radio frequency".
object AnnouncementHolder {
    // We will post new announcement strings to this LiveData
    val newAnnouncement = MutableLiveData<String>()
}