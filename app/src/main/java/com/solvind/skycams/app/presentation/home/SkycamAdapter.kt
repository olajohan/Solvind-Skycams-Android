package com.solvind.skycams.app.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.solvind.skycams.app.databinding.ListItemSkycamBinding
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.presentation.IHandleAlarm
import com.solvind.skycams.app.presentation.IProvideSkycamLiveData
import com.solvind.skycams.app.presentation.ads.IAdsProvider
import com.solvind.skycams.app.presentation.listener.AlarmOnClickListener
import com.solvind.skycams.app.presentation.navigation.INavigateToSingle

class SkycamAdapter(
    private val skycamLiveDataProvider: IProvideSkycamLiveData,
    private val alarmHandler: IHandleAlarm,
    private val fragment: Fragment,
    private val adsProvider: IAdsProvider
) : RecyclerView.Adapter<SkycamAdapter.SkycamViewHolder>() {

    private var skycamList: List<Skycam> = emptyList()

    inner class SkycamViewHolder(private val binding: ListItemSkycamBinding) : RecyclerView.ViewHolder(binding.root), INavigateToSingle {

        fun bind(item: Skycam)  {

            binding.apply {
                lifecycleOwner = fragment.viewLifecycleOwner
                listItemSkycam = item
                skycamLiveData = skycamLiveDataProvider.getSkycamLiveData(item.skycamKey)
                alarmLiveData = alarmHandler.getAlarmLiveData(item.skycamKey)
                activateAlarmButton.setOnClickListener(AlarmOnClickListener(alarmHandler, item.skycamKey))
                watchAdButton.setOnClickListener { adsProvider.showNextRewardedAd(fragment.requireActivity(), fragment.lifecycleScope, item) }
                root.setOnClickListener { navigateToSingleSkycam(binding.root, item) }
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkycamViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemSkycamBinding.inflate(inflater, parent, false)
        return SkycamViewHolder(binding)

    }

    fun replaceDatasetAndNotify(newSkycamList: List<Skycam>) {
        skycamList = newSkycamList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: SkycamViewHolder, position: Int) = holder.bind(skycamList[position])
    override fun getItemCount(): Int = skycamList.size

}