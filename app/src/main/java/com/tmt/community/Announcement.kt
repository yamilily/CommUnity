package com.tmt.community

// --- IMPORTS ---
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize // Add this annotation
data class Announcement(
    val title: String = "",
    val message: String = "",
    val date: String = "",
    val timestamp: Long = 0,
    val showInterestButton: Boolean = false
) : Parcelable // Implement Parcelable