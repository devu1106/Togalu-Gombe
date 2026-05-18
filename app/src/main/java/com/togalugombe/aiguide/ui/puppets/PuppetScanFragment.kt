package com.togalugombe.aiguide.ui.puppets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.togalugombe.aiguide.R
import com.togalugombe.aiguide.databinding.FragmentPuppetScanBinding

class PuppetScanFragment : Fragment() {

    private var _binding: FragmentPuppetScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PuppetsViewModel by viewModels()
    private lateinit var adapter: PuppetsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPuppetScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        
        // Fetch puppets
        viewModel.fetchPuppets()
    }

    private fun setupRecyclerView() {
        adapter = PuppetsAdapter { puppet ->
            val bundle = Bundle().apply {
                putString("puppetId", puppet.id)
            }
            findNavController().navigate(R.id.action_puppet_scan_to_details, bundle)
        }
        
        // Beautiful 2-column Grid Layout
        binding.rvPuppets.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPuppets.adapter = adapter
    }

    private fun observeViewModel() {
        // Observe Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbPuppetsLoader.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe Puppets List
        viewModel.puppets.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvPuppetsEmpty.visibility = View.VISIBLE
                adapter.submitList(emptyList())
            } else {
                binding.tvPuppetsEmpty.visibility = View.GONE
                adapter.submitList(list)
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
