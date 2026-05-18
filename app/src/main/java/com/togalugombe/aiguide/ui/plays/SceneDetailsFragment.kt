package com.togalugombe.aiguide.ui.plays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.togalugombe.aiguide.databinding.FragmentSceneDetailsBinding

class SceneDetailsFragment : Fragment() {

    private var _binding: FragmentSceneDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaysViewModel by viewModels()
    private var sceneId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sceneId = it.getString("sceneId") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSceneDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        
        // Fetch scene details
        if (sceneId.isNotEmpty()) {
            viewModel.fetchSceneDetails(sceneId)
        }
    }

    private fun observeViewModel() {
        viewModel.selectedScene.observe(viewLifecycleOwner) { scene ->
            if (scene != null) {
                binding.tvDetailsSceneTitle.text = scene.title
                binding.tvDetailsSceneDescription.text = scene.description
                
                // Coil loader
                binding.ivDetailsSceneImage.load(scene.imageUrl) {
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
