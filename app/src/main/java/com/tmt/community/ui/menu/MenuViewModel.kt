package com.tmt.community.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MenuViewModel : ViewModel() {

    private val _userEmail = MutableLiveData<String>()

    val userEmail: LiveData<String> = _userEmail

    init {
        val user = Firebase.auth.currentUser
        _userEmail.value = user?.email ?: "Not logged in"
    }
}