package com.togalugombe.aiguide.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import com.togalugombe.aiguide.R
import com.togalugombe.aiguide.databinding.FragmentHomeBinding
import com.togalugombe.aiguide.ui.plays.PlaysViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PlaysViewModel by viewModels()
    private lateinit var featuredAdapter: HomeFeaturedPlaysAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Fetch the plays to populate the top featured carousel
        viewModel.fetchPlays()
    }

    private fun setupRecyclerView() {
        featuredAdapter = HomeFeaturedPlaysAdapter { selectedPlay ->
            // Optionally navigate to play details if that feature is implemented later
            // For now, this handles the click event gracefully
        }
        binding.rvFeaturedPlays.adapter = featuredAdapter
        
        // Make the carousel snap perfectly to each item like a ViewPager
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvFeaturedPlays)
    }

    private fun observeViewModel() {
        viewModel.plays.observe(viewLifecycleOwner) { plays ->
            featuredAdapter.submitList(plays)
            if (plays.isNotEmpty()) {
                binding.pbFeaturedPlays.visibility = View.GONE
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading && featuredAdapter.itemCount == 0) {
                binding.pbFeaturedPlays.visibility = View.VISIBLE
            } else {
                binding.pbFeaturedPlays.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        // Grid item clicks
        binding.cardLiveAssist.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_live_assist)
        }

        binding.cardPuppetScan.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_puppet_scan)
        }

        binding.cardArtistConnect.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_artist_connect)
        }

        binding.cardProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
