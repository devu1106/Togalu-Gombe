package com.togalugombe.aiguide.ui.artists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.togalugombe.aiguide.R
import com.togalugombe.aiguide.data.model.Artist
import com.togalugombe.aiguide.databinding.ItemArtistBinding

class ArtistsAdapter(
    private var artists: List<Artist> = emptyList(),
    private val onCallClicked: (Artist) -> Unit
) : RecyclerView.Adapter<ArtistsAdapter.ArtistViewHolder>() {

    fun submitList(newList: List<Artist>) {
        artists = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artists[position])
    }

    override fun getItemCount(): Int = artists.size

    inner class ArtistViewHolder(private val binding: ItemArtistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.tvArtistName.text = artist.name
            binding.tvArtistWorkshop.text = artist.workshopDetails
            binding.tvArtistDescription.text = artist.description
            binding.tvArtistPhone.text = artist.phone
            
            // Coil circular avatar image loader with crossfade
            binding.ivArtistImage.load(artist.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }

            // Click call details trigger
            binding.tvArtistPhone.setOnClickListener {
                onCallClicked(artist)
            }
        }
    }
}
