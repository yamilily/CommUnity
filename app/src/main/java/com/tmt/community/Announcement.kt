package com.tmt.community

// This class structure is REQUIRED by Firebase for reading data.
class Announcement {
    var title: String = ""
    var message: String = ""
    var timestamp: Long = 0L
    var showInterestButton: Boolean = false
    var interestedUsers: MutableMap<String, Boolean> = mutableMapOf()

    // Firebase needs this empty constructor to create instances of the object.
    constructor() {}
}