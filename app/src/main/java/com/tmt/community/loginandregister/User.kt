package com.tmt.community.loginandregister

data class User(
    val email: String? = null,
    val role: String = "resident",
    val phoneNumber: String? = null  // e.g. "+639171234567"
)
