package com.solvind.skycams.app.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.solvind.skycams.app.databinding.ListItemSkycamBinding
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.presentation.IHandleAlarm
import com.solvind.skycams.app.presentation.IProvideRewardedAds
import com.solvind.skycams.app.presentation.IProvideSkycamLiveData
import com.solvind.skycams.app.presentation.RewardedAdState
import com.solvind.skycams.app.presentation.listener.AlarmOnClickListener
import com.solvind.skycams.app.presentation.navigation.INavigateToSingle
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SkycamAdapter(
    private val skycamLiveDataProvider: IProvideSkycamLiveData,
    private val alarmHandler: IHandleAlarm,
    private val fragment: Fragment,
    private val rewardedAdsProvider: IProvideRewardedAds
) : RecyclerView.Adapter<SkycamAdapter.SkycamViewHolder>() {

    private var skycamList: List<Skycam> = emptyList()

    inner class SkycamViewHolder(val binding: ListItemSkycamBinding) :
        RecyclerView.ViewHolder(binding.root), INavigateToSingle {

        fun bind(item: Skycam) {

            binding.apply {
                lifecycleOwner = fragment.viewLifecycleOwner
                listItemSkycam = item
                skycamLiveData = skycamLiveDataProvider.getSkycamLiveData(item.skycamKey)
                alarmLiveData = alarmHandler.getAlarmLiveData(item.skycamKey)
                activateAlarmButton.setOnClickListener(
                    AlarmOnClickListener(
                        alarmHandler,
                        item.skycamKey
                    )
                )
                root.setOnClickListener { navigateToSingleSkycam(binding.root, item) }
            }


            rewardedAdsProvider.rewardedAdState.observe(fragment.viewLifecycleOwner, { rewardedAdState ->
                    when (rewardedAdState) {

                        /**
                         * When the rewarded ad is being downloaded display a loading indicator
                         * */
                        RewardedAdState.Loading -> binding.watchAdButton.apply {
                            isEnabled = false
                            text = "Loading..."
                        }

                        /**
                         * When the rewarded ad has been loaded and is ready to be shown, change
                         * the text of the button to "Watch ad" and set the on click listener to
                         * display the ad.
                         * */
                        is RewardedAdState.Ready -> binding.watchAdButton.apply {
                            isEnabled = true
                            text = "Ad +30min"
                            setOnClickListener {
                                rewardedAdState.ad.show(
                                    fragment.requireActivity(),
                                    object : RewardedAdCallback() {
                                        override fun onUserEarnedReward(reward: RewardItem) {
                                            val rewardedSeconds = TimeUnit.MINUTES.toSeconds(reward.amount.toLong())
                                            Timber.i("Rewarded seconds: $rewardedSeconds")
                                            rewardedAdsProvider.rewardUserAlarmTime(
                                                item.skycamKey,
                                                TimeUnit.MINUTES.toSeconds(reward.amount.toLong())
                                            )
                                        }
                                    })
                            }
                        }

                        /**
                         * Disable the watch ad button if the ad fails to laod. The rewarded ads provider
                         * will take care of loading a new ad once the internet connection is back.
                         * */
                        is RewardedAdState.Failure -> binding.watchAdButton.apply {
                            Timber.i("${rewardedAdState.adError}")
                            setOnClickListener {}
                            isEnabled = false
                            text = "Not available"
                        }
                    }
                })

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

    override fun onBindViewHolder(holder: SkycamViewHolder, position: Int) {

        /**
         * We set the image drawable in the imageview to null to prevent coil from loading the
         * image into the wrong viewholder while the user is scrolling the recyclerview.
         * */
        holder.binding.liveViewImageView.setImageDrawable(null)
        holder.binding.iconImageView.setImageDrawable(null)

        holder.bind(skycamList[position])
    }

    override fun getItemCount(): Int = skycamList.size

}