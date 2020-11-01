package com.solvind.skycams.app.presentation.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.solvind.skycams.app.R
import com.solvind.skycams.app.presentation.MainViewModel
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
    private val mMainViewModel: MainViewModel by activityViewModels()

    private lateinit var mConnectivityManager: ConnectivityManager

    /**
     * Refreshes the skycamlist when it has an active internet connection
     * */
    private val mDefaultNetworkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                lifecycleScope.launchWhenStarted { mViewModel.refreshSkycamList() }
        }
    }

    /**
     * As we need the activity context, we must wait with the initialization of the connectivity manager
     * until we are sure the fragment is attached to a activity.
     * */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mConnectivityManager = context.getSystemService(ConnectivityManager::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSkycamAdapter = SkycamAdapter(
            mViewModel,
            mViewModel,
            this,
            mMainViewModel
        )
        val navController = findNavController()
        val appBarConfigureation = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfigureation)

        view.skycam_recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mSkycamAdapter
        }

        mViewModel.mainViewStateReadOnly.observe(viewLifecycleOwner) {
            when (it) {
                HomeViewModel.MainViewState.Loading -> Timber.i("Loading")
                is HomeViewModel.MainViewState.Success -> mSkycamAdapter.replaceDatasetAndNotify(it.skycamList)
                is HomeViewModel.MainViewState.Failed -> Timber.i("Failed: ${it.failure}")
            }
        }

        mMainViewModel.internetConnectionType.observe(viewLifecycleOwner, {
            Timber.i("Connection type = ${it.javaClass.name}")
        })
    }

    override fun onResume() {
        super.onResume()
        mConnectivityManager.registerDefaultNetworkCallback(mDefaultNetworkCallback)
    }

    override fun onPause() {
        super.onPause()
        mConnectivityManager.unregisterNetworkCallback(mDefaultNetworkCallback)
    }
}