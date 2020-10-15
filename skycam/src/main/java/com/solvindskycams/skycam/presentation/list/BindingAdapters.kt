package com.solvindskycams.skycam.presentation.list

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.firebase.storage.FirebaseStorage
import com.solvindskycams.skycam.domain.model.Skycam

import io.github.rosariopfernandes.firecoil.load
import timber.log.Timber


object BindingAdapters {

    @BindingAdapter("skycam")
    @JvmStatic fun loadImage(view: ImageView, skycam: Skycam) {

        Timber.i("Skycam mainImage = ${skycam.mainImage}")
        if (skycam.mostRecentImageId.isNotEmpty()) {
            val storageRef = FirebaseStorage.getInstance().getReference(skycam.mainImage)
            view.load(storageRef)
        }
    }



}
