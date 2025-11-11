package com.tmt.community.ui.home // Make sure this matches your package

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.tmt.community.databinding.FragmentHomeBinding // Make sure this matches

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Get a reference to our Firebase Functions
    private val functions = Firebase.functions

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set a click listener on our send button
        binding.buttonSend.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            val body = binding.editTextBody.text.toString().trim()

            if (title.isNotEmpty() && body.isNotEmpty()) {
                sendAnnouncement(title, body)
            } else {
                Toast.makeText(requireContext(), "Title and body cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun sendAnnouncement(title: String, body: String) {
        // Create the data to send to the function. This is the simple, correct structure.
        val data = hashMapOf(
            "title" to title,
            "body" to body
        )

        binding.buttonSend.isEnabled = false // Disable button to prevent spamming

        // Call the function and handle the result
        functions
            .getHttpsCallable("sendTownAnnouncement")
            .call(data) // We pass this simple HashMap directly. The SDK handles the wrapping.
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    Log.w("FunctionCall", "Function failed", e)
                    Toast.makeText(requireContext(), "Error: ${e?.message}", Toast.LENGTH_LONG).show()
                } else {
                    // It was successful!
                    Log.d("FunctionCall", "Function success!")
                    Toast.makeText(requireContext(), "Announcement sent!", Toast.LENGTH_SHORT).show()
                    binding.editTextTitle.text?.clear()
                    binding.editTextBody.text?.clear()
                }
                binding.buttonSend.isEnabled = true // Re-enable button
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}