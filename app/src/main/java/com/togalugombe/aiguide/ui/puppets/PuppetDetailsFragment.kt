package com.togalugombe.aiguide.ui.puppets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.togalugombe.aiguide.databinding.FragmentPuppetDetailsBinding

class PuppetDetailsFragment : Fragment() {

    private var _binding: FragmentPuppetDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PuppetsViewModel by viewModels()
    private var puppetId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            puppetId = it.getString("puppetId") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPuppetDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        
        // Fetch details
        if (puppetId.isNotEmpty()) {
            viewModel.fetchPuppetDetails(puppetId)
        }
    }

    private fun observeViewModel() {
        viewModel.selectedPuppet.observe(viewLifecycleOwner) { puppet ->
            if (puppet != null) {
                binding.tvDetailsPuppetName.text = puppet.name
                binding.tvDetailsPuppetPowers.text = puppet.powers
                binding.tvDetailsPuppetSymbolism.text = puppet.symbolism
                binding.tvDetailsPuppetDescription.text = puppet.description
                
                // Coil loader
                binding.ivDetailsPuppetImage.load(puppet.imageUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                }
            }
        }

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
