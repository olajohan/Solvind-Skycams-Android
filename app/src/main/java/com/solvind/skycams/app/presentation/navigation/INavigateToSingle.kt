package com.solvind.skycams.app.presentation.navigation

import android.view.View
import androidx.navigation.findNavController
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.presentation.single.SingleSkycamFragmentDirections

interface INavigateToSingle {

    fun navigateToSingleSkycam(view: View, skycam: Skycam) {
        view.findNavController().navigate(
            SingleSkycamFragmentDirections.actionNavigateToSingle(
            skycamKey = skycam.skycamKey,
            skycamName = skycam.location.name,
            skycamMainImage = skycam.mainImage
        ))
    }
}