package com.togalugombe.aiguide.ui.plays

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.togalugombe.aiguide.R
import com.togalugombe.aiguide.data.model.Scene
import com.togalugombe.aiguide.databinding.ItemSceneBinding

class ScenesAdapter(
    private var scenes: List<Scene> = emptyList(),
    private val onSceneClicked: (Scene) -> Unit
) : RecyclerView.Adapter<ScenesAdapter.SceneViewHolder>() {

    fun submitList(newList: List<Scene>) {
        scenes = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SceneViewHolder {
        val binding = ItemSceneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SceneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SceneViewHolder, position: Int) {
        holder.bind(scenes[position])
    }

    override fun getItemCount(): Int = scenes.size

    inner class SceneViewHolder(private val binding: ItemSceneBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(scene: Scene) {
            binding.tvSceneTitle.text = scene.title
            binding.tvSceneOrder.text = "Scene ${scene.orderNumber}"
            
            // Coil async image loader with crossfade
            binding.ivSceneImage.load(scene.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }

            binding.root.setOnClickListener {
                onSceneClicked(scene)
            }
        }
    }
}
