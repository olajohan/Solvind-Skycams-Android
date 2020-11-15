package com.solvind.skycams.app.presentation.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.solvind.skycams.app.presentation.ads.AdProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeFragmentFactory @Inject constructor(
    private val adProvider: AdProvider
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when(className) {
            HomeFragment::class.java.name -> HomeFragment(adProvider)
            else -> return super.instantiate(classLoader, className)
        }
    }
}