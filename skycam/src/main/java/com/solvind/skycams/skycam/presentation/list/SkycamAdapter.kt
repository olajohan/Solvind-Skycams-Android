package com.solvind.skycams.skycam.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.solvind.skycams.skycam.databinding.SkycamListItemBinding
import com.solvind.skycams.skycam.domain.model.Skycam

class SkycamAdapter(private var skycamList: List<Skycam>, private val fragment: Fragment) : RecyclerView.Adapter<SkycamAdapter.SkycamViewHolder>() {

    inner class SkycamViewHolder(private val binding: SkycamListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Skycam)  {
            binding.skycam = item
            binding.root.setOnClickListener {
                if (fragment is HomeFragment) fragment.navigateToSingleSkycam(item)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkycamViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SkycamListItemBinding.inflate(inflater, parent, false)
        return SkycamViewHolder(binding)

    }

    fun replaceDatasetAndNotify(newSkycamList: List<Skycam>) {
        skycamList = newSkycamList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: SkycamViewHolder, position: Int) = holder.bind(skycamList[position])
    override fun getItemCount(): Int = skycamList.size

}