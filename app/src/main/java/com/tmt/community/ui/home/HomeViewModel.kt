package com.tmt.community.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tmt.community.loginandregister.User

class HomeViewModel : ViewModel() {

    private val _userRole = MutableLiveData<String>()

    val userRole: LiveData<String> = _userRole

    init {
        fetchUserRole()
    }

    private fun fetchUserRole() {
        val firebaseUser = Firebase.auth.currentUser
        if (firebaseUser == null) {
            _userRole.value = "guest"
            return
        }

        val userId = firebaseUser.uid
        val database = Firebase.database.reference.child("users").child(userId)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    _userRole.value = user?.role ?: "resident"
                } else {
                    _userRole.value = "resident"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _userRole.value = "resident"
            }
        })
    }
}