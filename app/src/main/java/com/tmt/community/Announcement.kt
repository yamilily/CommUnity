package com.tmt.community

// Add a no-argument constructor for Firebase
data class Announcement(
    val title: String = "",
    val body: String = "",
    val timestamp: Long = 0
)