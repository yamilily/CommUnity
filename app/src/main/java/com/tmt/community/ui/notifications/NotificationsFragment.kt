package com.tmt.community.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.SimpleItemAnimator
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.tmt.community.Announcement
import com.tmt.community.AnnouncementHolder
import com.tmt.community.databinding.AnnouncementItemBinding
import com.tmt.community.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private var adapter: FirebaseRecyclerAdapter<Announcement, AnnouncementHolder>? = null

    // Variable to store if the current user is an admin
    private var isAdmin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // 1. Fetch the user's role first
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance("https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").getValue(String::class.java)
                isAdmin = (role == "admin")
                // Notify adapter to refresh views with new permission
                adapter?.notifyDataSetChanged()
            }
        }

        // 2. Setup the adapter
        val database = FirebaseDatabase.getInstance("https://community-1f98e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Announcements")
        val query = database.orderByChild("timestamp")

        val options = FirebaseRecyclerOptions.Builder<Announcement>()
            .setQuery(query, Announcement::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<Announcement, AnnouncementHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementHolder {
                val itemBinding = AnnouncementItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AnnouncementHolder(itemBinding)
            }

            override fun onBindViewHolder(holder: AnnouncementHolder, position: Int, model: Announcement) {
                val reversedPosition = itemCount - 1 - position
                val announcementRef = getRef(reversedPosition)

                // 3. Pass the isAdmin flag to the holder
                holder.bind(getItem(reversedPosition), announcementRef, isAdmin)
            }

            override fun onDataChanged() {
                super.onDataChanged()
                binding.emptyView.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
            }
        }

        (binding.recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        binding.recyclerView.adapter = adapter
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}