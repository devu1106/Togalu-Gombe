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
import com.togalugombe.aiguide.databinding.FragmentScenesBinding

class ScenesFragment : Fragment() {

    private var _binding: FragmentScenesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaysViewModel by viewModels()
    private lateinit var adapter: ScenesAdapter
    
    private var playId: String = ""
    private var playTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playId = it.getString("playId") ?: ""
            playTitle = it.getString("playTitle") ?: "Play Scenes"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScenesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitleScenes.text = playTitle
        
        setupRecyclerView()
        observeViewModel()
        
        // Fetch scenes
        if (playId.isNotEmpty()) {
            viewModel.fetchScenesForPlay(playId)
        }
    }

    private fun setupRecyclerView() {
        adapter = ScenesAdapter { scene ->
            val bundle = Bundle().apply {
                putString("sceneId", scene.id)
            }
            findNavController().navigate(R.id.action_scenes_to_details, bundle)
        }
        
        binding.rvScenes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScenes.adapter = adapter
    }

    private fun observeViewModel() {
        // Observe Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbScenesLoader.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe Scenes
        viewModel.scenes.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvScenesEmpty.visibility = View.VISIBLE
                adapter.submitList(emptyList())
            } else {
                binding.tvScenesEmpty.visibility = View.GONE
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
