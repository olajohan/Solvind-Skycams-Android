package com.solvind.skycams.app.presentation

import android.widget.Button
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.clear
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.presentation.home.TimeSinceChronometer
import io.github.rosariopfernandes.firecoil.load
import timber.log.Timber
import java.time.Instant


object BindingAdapters {

    private val skycamImagesStorage = Firebase.storage("gs://skycam-images")
    private val auroraAlarmAppStorage = Firebase.storage("gs://aurora-alarm.appspot.com")

    @JvmStatic
    @BindingAdapter("load_skycam_image")
    fun loadSkycamImage(view: ImageView, storageLocation: String?) {

        /**
         * Clear any pending requests on the image view. This is to prevent the recyclerview from
         * accidentaly loading the wrong image into a recycled view.
         * */
        view.clear()

        if (storageLocation != null) {
            when {
                storageLocation.startsWith("gs://skycam-images") -> {
                    val storageRef = skycamImagesStorage.getReferenceFromUrl(storageLocation)
                    view.load(storageRef) {
                        crossfade(true)
                        memoryCachePolicy(CachePolicy.DISABLED)
                        diskCachePolicy(CachePolicy.DISABLED)
                        networkCachePolicy(CachePolicy.DISABLED)
                    }
                }
                else -> Timber.i("Unknown storage location for image $storageLocation")
            }
        }
    }

    @JvmStatic
    @BindingAdapter("load_main_image")
    fun loadMainImage(view: ImageView, storageLocation: String?) {

        /**
         * Clear any pending requests on the image view. This is to prevent the recyclerview from
         * accidentaly loading the wrong image into a recycled view.
         * */
        view.clear()

        if (storageLocation != null) {
            when {
                storageLocation.startsWith("gs://aurora-alarm.appspot.com") -> {
                    val storageRef = auroraAlarmAppStorage.getReferenceFromUrl(storageLocation)
                    view.load(storageRef)
                }
            }
        }
    }

    @JvmStatic
    @BindingAdapter("load_main_image_thumb")
    fun loadMainImageThumb(view: ImageView, storageLocation: String?) {

        /**
         * Clear any pending requests on the image view. This is to prevent the recyclerview from
         * accidentaly loading the wrong image into a recycled view.
         * */
        view.clear()

        if (storageLocation != null) {
            when {
                storageLocation.startsWith("gs://aurora-alarm.appspot.com") -> {
                    val storageRef = auroraAlarmAppStorage.getReferenceFromUrl(storageLocation)
                    view.load(storageRef) {
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }
    }

    @JvmStatic
    @BindingAdapter("reset_chronometer")
    fun convertTimestampToTimeSince(view: TimeSinceChronometer, epochSeconds: Long) = view.reset(epochSeconds)

    @JvmStatic
    @BindingAdapter("set_alarm_button_enabled_state")
    fun setAlarmButtonEnabledState(view: Button, alarmAvailableUntilEpcohSeconds: Long) {
        view.isEnabled = alarmAvailableUntilEpcohSeconds > Instant.now().epochSecond || alarmAvailableUntilEpcohSeconds == 0L
    }

    @JvmStatic
    @BindingAdapter("set_alarm_button_text")
    fun setAlarmButtonText(view: Button, alarmConfig: AlarmConfig) {

    }

}
