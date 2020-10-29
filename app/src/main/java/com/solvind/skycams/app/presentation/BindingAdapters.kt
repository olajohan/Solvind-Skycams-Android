package com.solvind.skycams.app.presentation

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.transform.CircleCropTransformation
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.solvind.skycams.app.presentation.home.TimeSinceChronometer
import io.github.rosariopfernandes.firecoil.load
import timber.log.Timber


object BindingAdapters {

    private val skycamImagesStorage = Firebase.storage("gs://skycam-images")
    private val auroraAlarmAppStorage = Firebase.storage("gs://aurora-alarm.appspot.com")

    @JvmStatic
    @BindingAdapter("load_skycam_image")
    fun loadSkycamImage(view: ImageView, storageLocation: String?) {

        if (storageLocation != null) {
            when {
                storageLocation.startsWith("gs://skycam-images") -> {
                    val storageRef = skycamImagesStorage.getReferenceFromUrl(storageLocation)
                    view.load(storageRef) {
                        crossfade(true)
                    }
                }
                else -> Timber.i("Unknown storage location for image $storageLocation")
            }
        }
    }

    @JvmStatic
    @BindingAdapter("load_main_image")
    fun loadMainImage(view: ImageView, storageLocation: String?) {
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

}
