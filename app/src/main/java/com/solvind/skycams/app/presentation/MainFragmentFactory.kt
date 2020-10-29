package com.solvind.skycams.app.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.solvind.skycams.app.presentation.home.HomeFragment
import javax.inject.Inject

class MainFragmentFactory @Inject constructor() : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {

        return when(className) {
            HomeFragment::class.java.name -> HomeFragment()
            // Add more fragments here

            else -> return super.instantiate(classLoader, className)
        }
    }
}