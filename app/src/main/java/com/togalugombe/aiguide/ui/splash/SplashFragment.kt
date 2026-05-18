package com.togalugombe.aiguide.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.togalugombe.aiguide.R
import com.togalugombe.aiguide.databinding.FragmentSplashBinding
import com.togalugombe.aiguide.ui.auth.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check user session
        authViewModel.checkUserSession()

        // Wait 2 seconds and navigate dynamically
        lifecycleScope.launch {
            delay(2000)
            val currentUser = authViewModel.userState.value
            if (currentUser != null) {
                // User is authenticated, route to Home dashboard
                findNavController().navigate(R.id.action_splash_to_home)
            } else {
                // No session found, route to Login form
                findNavController().navigate(R.id.action_splash_to_login)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
