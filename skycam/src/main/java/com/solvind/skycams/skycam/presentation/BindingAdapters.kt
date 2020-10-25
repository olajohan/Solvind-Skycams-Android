package com.solvind.skycams.skycam.presentation

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import io.github.rosariopfernandes.firecoil.load
import timber.log.Timber


object BindingAdapters {

    private val skycamImagesStorage = Firebase.storage("gs://skycam-images")
    private val auroraAlarmAppStorage = Firebase.storage("gs://aurora-alarm.appspot.com")

    @JvmStatic
    @BindingAdapter("load_skycam_image")
    fun loadSkycamImage(view: ImageView, storageLocation: String?) {

        Timber.i("Trying to load image from storage location: $storageLocation")
        if (!storageLocation.isNullOrEmpty()) {

            val storageRef = when {
                storageLocation.startsWith("gs://skycam-images") -> skycamImagesStorage.getReferenceFromUrl(storageLocation)
                storageLocation.startsWith("gs://aurora-alarm.appspot.com") -> auroraAlarmAppStorage.getReferenceFromUrl(storageLocation)
                else -> null
            }

            if (storageRef != null) view.load(storageRef)
        }
    }
}
