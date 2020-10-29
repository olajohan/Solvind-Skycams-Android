package com.solvind.skycams.app.presentation.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.solvind.skycams.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.view.*
import timber.log.Timber


/**
 * Presents all the skycams a list to the user. When the user clicks one of the skycams it should
 * take the user to single skycam fragment
 */
@AndroidEntryPoint
class HomeFragment() : Fragment(R.layout.fragment_home) {

    private lateinit var mSkycamAdapter: SkycamAdapter
    private val mViewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSkycamAdapter = SkycamAdapter(mViewModel, mViewModel, viewLifecycleOwner)
        val navController = findNavController()
        val appBarConfigureation = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.toolbar).setupWithNavController(navController, appBarConfigureation)

        view.skycam_recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mSkycamAdapter
        }

        mViewModel.viewStateReadOnly.observe(viewLifecycleOwner) {
            when(it) {
                HomeViewModel.ViewState.Loading -> Timber.i("Loading")
                is HomeViewModel.ViewState.Success -> mSkycamAdapter.replaceDatasetAndNotify(it.skycamList)
                is HomeViewModel.ViewState.Failed -> Timber.i("Failed")
            }
        }
    }
}