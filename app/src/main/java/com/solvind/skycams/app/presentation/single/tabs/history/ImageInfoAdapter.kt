package com.solvind.skycams.app.presentation.single.tabs.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.solvind.skycams.app.databinding.ListItemImageInfoBinding
import com.solvind.skycams.app.domain.model.ImageInfo


class ImageInfoAdapter(private var dataSet: MutableList<ImageInfo>) : RecyclerView.Adapter<ImageInfoAdapter.SkycamImageDetailsViewHolder>() {

    inner class SkycamImageDetailsViewHolder(private val binding: ListItemImageInfoBinding): RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(item: ImageInfo)  {
            binding.skycamImageDetails = item
            binding.executePendingBindings()
        }
    }

    fun addNewImage(imageInfo: ImageInfo) {
        dataSet.add(0, imageInfo)
        notifyItemInserted(0)
    }

    fun replaceDataset(newList: List<ImageInfo>) {
        dataSet = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SkycamImageDetailsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemImageInfoBinding.inflate(inflater, parent, false)
        return SkycamImageDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkycamImageDetailsViewHolder, position: Int) {
        holder.bind(dataSet[position])

    }

    override fun getItemCount(): Int = dataSet.size

}