package com.togalugombe.aiguide.ui.plays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.togalugombe.aiguide.R
import com.togalugombe.aiguide.databinding.FragmentLiveAssistBinding

class LiveAssistFragment : Fragment() {

    private var _binding: FragmentLiveAssistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaysViewModel by viewModels()
    private lateinit var adapter: PlaysAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveAssistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        
        // Fetch plays
        viewModel.fetchPlays()
    }

    private fun setupRecyclerView() {
        adapter = PlaysAdapter { play ->
            // Clicks open Scenes Screen passing arguments
            val bundle = Bundle().apply {
                putString("playId", play.id)
                putString("playTitle", play.title)
            }
            findNavController().navigate(R.id.action_live_assist_to_scenes, bundle)
        }
        
        binding.rvPlays.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlays.adapter = adapter
    }

    private fun observeViewModel() {
        // Observe Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbPlaysLoader.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe Plays List
        viewModel.plays.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvPlaysEmpty.visibility = View.VISIBLE
                adapter.submitList(emptyList())
            } else {
                binding.tvPlaysEmpty.visibility = View.GONE
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
