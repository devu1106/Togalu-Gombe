package com.togalugombe.aiguide.ui.plays

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.togalugombe.aiguide.R
import com.togalugombe.aiguide.data.model.Play
import com.togalugombe.aiguide.databinding.ItemPlayBinding

class PlaysAdapter(
    private var plays: List<Play> = emptyList(),
    private val onPlayClicked: (Play) -> Unit
) : RecyclerView.Adapter<PlaysAdapter.PlayViewHolder>() {

    fun submitList(newList: List<Play>) {
        plays = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayViewHolder {
        val binding = ItemPlayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayViewHolder, position: Int) {
        holder.bind(plays[position])
    }

    override fun getItemCount(): Int = plays.size

    inner class PlayViewHolder(private val binding: ItemPlayBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(play: Play) {
            binding.tvPlayTitle.text = play.title
            binding.tvPlayDescription.text = play.description
            
            // Coil async image loader with fallback loaders
            binding.ivPlayImage.load(play.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }

            binding.root.setOnClickListener {
                onPlayClicked(play)
            }
        }
    }
}
