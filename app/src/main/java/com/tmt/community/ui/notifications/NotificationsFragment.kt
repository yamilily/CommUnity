package com.tmt.community.ui.notifications

// --- ALL NECESSARY IMPORTS ---
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.tmt.community.Announcement
import com.tmt.community.AnnouncementHolder
import com.tmt.community.databinding.FragmentNotificationsBinding
import com.tmt.community.databinding.AnnouncementItemBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FirebaseRecyclerAdapter<Announcement, AnnouncementHolder>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup RecyclerView
        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.announcementsRecyclerView.layoutManager = layoutManager

        // 2. Create the Firebase Query
        val query = FirebaseDatabase.getInstance()
            .reference
            .child("Announcements")
            .orderByChild("timestamp")

        // 3. Configure the FirebaseRecyclerOptions
        val options = FirebaseRecyclerOptions.Builder<Announcement>()
            .setQuery(query, Announcement::class.java)
            .build()

        // 4. Create the FirebaseRecyclerAdapter
        adapter = object : FirebaseRecyclerAdapter<Announcement, AnnouncementHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementHolder {
                val itemBinding = AnnouncementItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AnnouncementHolder(itemBinding)
            }

            override fun onBindViewHolder(holder: AnnouncementHolder, position: Int, model: Announcement) {
                holder.bind(model)
            }
        }

        // 5. Set the adapter on the RecyclerView
        binding.announcementsRecyclerView.adapter = adapter
    }

    // --- Lifecycle Methods for the Adapter ---
    override fun onStart() {
        super.onStart()
        if (::adapter.isInitialized) {
            adapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::adapter.isInitialized) {
            adapter.stopListening()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}