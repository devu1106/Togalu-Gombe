package com.togalugombe.aiguide.ui.artists

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.togalugombe.aiguide.databinding.FragmentArtistConnectBinding

class ArtistConnectFragment : Fragment() {

    private var _binding: FragmentArtistConnectBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArtistsViewModel by viewModels()
    private lateinit var adapter: ArtistsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        
        // Fetch artists
        viewModel.fetchArtists()
    }

    private fun setupRecyclerView() {
        adapter = ArtistsAdapter { artist ->
            // Trigger dialing contact phone via ACTION_DIAL intent (extremely secure, doesn't require hard permissions)
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${artist.phone}")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to open phone dialer", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.rvArtists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArtists.adapter = adapter
    }

    private fun observeViewModel() {
        // Observe Loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbArtistsLoader.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe Artists
        viewModel.artists.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvArtistsEmpty.visibility = View.VISIBLE
                adapter.submitList(emptyList())
            } else {
                binding.tvArtistsEmpty.visibility = View.GONE
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
