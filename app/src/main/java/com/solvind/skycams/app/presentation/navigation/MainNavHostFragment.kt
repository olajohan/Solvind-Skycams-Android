package com.solvind.skycams.app.presentation.navigation

import android.content.Context
import androidx.navigation.fragment.NavHostFragment
import com.solvind.skycams.app.presentation.home.HomeFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created to allow fragment constructor injection for fragments in the navgraph
 * */
@AndroidEntryPoint
class MainNavHostFragment : NavHostFragment() {

    @Inject
    lateinit var fragmentFactory: HomeFragmentFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
    }
}