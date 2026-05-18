package com.togalugombe.aiguide.ui.puppets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.togalugombe.aiguide.R
import com.togalugombe.aiguide.data.model.Puppet
import com.togalugombe.aiguide.databinding.ItemPuppetBinding

class PuppetsAdapter(
    private var puppets: List<Puppet> = emptyList(),
    private val onPuppetClicked: (Puppet) -> Unit
) : RecyclerView.Adapter<PuppetsAdapter.PuppetViewHolder>() {

    fun submitList(newList: List<Puppet>) {
        puppets = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuppetViewHolder {
        val binding = ItemPuppetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PuppetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PuppetViewHolder, position: Int) {
        holder.bind(puppets[position])
    }

    override fun getItemCount(): Int = puppets.size

    inner class PuppetViewHolder(private val binding: ItemPuppetBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(puppet: Puppet) {
            binding.tvPuppetName.text = puppet.name
            
            // Coil async image loader with crossfade
            binding.ivPuppetImage.load(puppet.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }

            binding.root.setOnClickListener {
                onPuppetClicked(puppet)
            }
        }
    }
}
