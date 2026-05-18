package com.togalugombe.aiguide.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.togalugombe.aiguide.R
import com.togalugombe.aiguide.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
        
        // Fetch profile
        viewModel.fetchUserProfile()
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            // Simply execute sign out
            viewModel.logout()
        }
    }

    private fun observeViewModel() {
        // Observe Profile details loading
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvProfileName.text = user.name
                binding.tvProfileEmail.text = user.email
            }
        }

        // Observe Logout Success Status
        viewModel.isLoggedOut.observe(viewLifecycleOwner) { isLoggedOut ->
            if (isLoggedOut) {
                Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
                // Route user back to authentication login flow
                findNavController().navigate(R.id.action_profile_to_login)
            }
        }

        // Observe Errors
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
