package com.solvindskycams.skycam.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.solvindskycams.skycam.databinding.SkycamListItemBinding
import com.solvindskycams.skycam.domain.model.Skycam

class SkycamAdapter(private var skycamList: List<Skycam>) : RecyclerView.Adapter<SkycamAdapter.SkycamViewHolder>() {

    inner class SkycamViewHolder(private val binding: SkycamListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Skycam)  {
            binding.skycam = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkycamViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SkycamListItemBinding.inflate(inflater, parent, false)
        return SkycamViewHolder(binding)

    }

    fun updateData(newSkycamList: List<Skycam>) {
        skycamList = newSkycamList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: SkycamViewHolder, position: Int) = holder.bind(skycamList[position])
    override fun getItemCount(): Int = skycamList.size

}