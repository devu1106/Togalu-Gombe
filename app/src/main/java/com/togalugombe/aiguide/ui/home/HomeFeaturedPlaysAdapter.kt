package com.togalugombe.aiguide.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.togalugombe.aiguide.data.model.Play
import com.togalugombe.aiguide.databinding.ItemFeaturedPlayHomeBinding

class HomeFeaturedPlaysAdapter(
    private var plays: List<Play> = emptyList(),
    private val onPlayClicked: (Play) -> Unit
) : RecyclerView.Adapter<HomeFeaturedPlaysAdapter.FeaturedPlayViewHolder>() {

    fun submitList(newList: List<Play>) {
        plays = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedPlayViewHolder {
        val binding = ItemFeaturedPlayHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeaturedPlayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeaturedPlayViewHolder, position: Int) {
        holder.bind(plays[position])
    }

    override fun getItemCount(): Int = plays.size

    inner class FeaturedPlayViewHolder(private val binding: ItemFeaturedPlayHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(play: Play) {
            binding.tvPlayTitle.text = play.title
            binding.tvPlayDescription.text = play.description
            
            // Coil async image loader for dynamic network images
            if (play.imageUrl.isNotEmpty()) {
                binding.ivPlayThumbnail.load(play.imageUrl) {
                    crossfade(true)
                    placeholder(android.R.color.darker_gray)
                    error(android.R.color.darker_gray)
                }
            }

            binding.root.setOnClickListener {
                onPlayClicked(play)
            }
        }
    }
}
