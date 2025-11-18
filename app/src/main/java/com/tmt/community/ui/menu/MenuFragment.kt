package com.tmt.community.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.tmt.community.databinding.FragmentMenuBinding
import com.tmt.community.loginandregister.LoginActivity

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var menuViewModel: MenuViewModel
    private lateinit var auth: FirebaseAuth

    // STEP 1: Simplify onCreateView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        menuViewModel = ViewModelProvider(this).get(MenuViewModel::class.java)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    // STEP 2: Move all logic into onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the user's email
        menuViewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.userEmailText.text = email
        }

        // Set up the logout button
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}