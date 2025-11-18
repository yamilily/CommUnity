package com.tmt.community.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MenuViewModel : ViewModel() {

    // Private variable that can be changed
    private val _userEmail = MutableLiveData<String>()

    // Public, unchangeable version that the Fragment will observe
    val userEmail: LiveData<String> = _userEmail

    init {
        // When the ViewModel is created, get the user's email
        val user = Firebase.auth.currentUser
        _userEmail.value = user?.email ?: "Not logged in"
    }
}