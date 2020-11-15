package com.solvind.skycams.app.presentation.home

import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.solvind.skycams.app.R
import com.solvind.skycams.app.databinding.FragmentHomeBinding
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.presentation.ads.AdProvider
import com.solvind.skycams.app.presentation.ads.AdState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi


/**
 * Presents all the skycams a list to the user. When the user clicks one of the skycams it should
 * take the user to single skycam fragment
 */
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment(
    private val mAdProvider: AdProvider
) : Fragment() {

    private val mViewModel: HomeViewModel by viewModels()
    private lateinit var mMapView: MapView
    private lateinit var mBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentHomeBinding.inflate(inflater)
        mBinding.viewModel = mViewModel
        mBinding.lifecycleOwner = viewLifecycleOwner

        mViewModel.skycamBottomSheetViewState.observe(
            viewLifecycleOwner,
            { skycamBottomSheetViewState ->
                BottomSheetBehavior.from(skycam_bottom_sheet).state = skycamBottomSheetViewState.state
            })

        mViewModel.alarmButtonViewState.observe(viewLifecycleOwner) { state ->
            when (state) {

                HomeViewModel.AlarmButtonViewState.Loading -> { }

                is HomeViewModel.AlarmButtonViewState.Activated -> {
                    mBinding.fabAlarm.apply {
                        setOnClickListener { mViewModel.deactivateAlarm(state.alarmConfig.skycamKey) }
                        icon = Icon.createWithResource(
                            requireContext(),
                            R.drawable.ic_baseline_alarm_on_24
                        ).loadDrawable(requireContext())
                        extend()
                    }
                }

                is HomeViewModel.AlarmButtonViewState.Deactivated -> mBinding.fabAlarm.apply {
                    setOnClickListener { mViewModel.activateAlarm(state.alarmConfig.skycamKey) }
                    icon = Icon.createWithResource(
                        requireContext(),
                        R.drawable.ic_baseline_alarm_add_24
                    ).loadDrawable(requireContext())
                    text = ""
                    shrink()
                }
                is HomeViewModel.AlarmButtonViewState.TimedOut -> mBinding.fabAlarm.apply {
                    setOnClickListener {
                        Toast.makeText(requireContext(), "Watch ads to gain more alarm minutes", Toast.LENGTH_LONG).show()
                    }
                    shrink()
                    icon = Icon.createWithResource(
                        requireContext(),
                        R.drawable.ic_baseline_alarm_off_24
                    ).loadDrawable(requireContext())
                    text = ""
                }
            }
        }

        mAdProvider.adState.observe(viewLifecycleOwner) {
            when (it) {
                AdState.Loading -> {
                    mBinding.adLoadingProgress.visibility = View.VISIBLE
                }
                else -> {
                    mBinding.adLoadingProgress.visibility = View.GONE
                }
            }
        }

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        mMapView = requireActivity().findViewById(R.id.mapView)
        mMapView.onCreate(mapViewBundle)
        lifecycleScope.launchWhenCreated {

            /**
             * The google map will be safe to use after the call to awaitmap.
             * */
            val googleMap = mMapView.awaitMap()

            /**
             * Populate the map with skycam markers
             * */
            mViewModel.skycams.observe(viewLifecycleOwner, {
                addSkycamsToMap(googleMap, it)
            })

            googleMap.setOnMarkerClickListener(MapMarkerOnClickListener())
            googleMap.setOnMapClickListener(MapClickListener())

            /**
             * Consume the click, so it will not get passed down to the map which hides the bottom sheet
             * when it receives a click
             * */
            mBinding.skycamBottomSheet.setOnClickListener {}

            /**
             * Load an ad, so the user won't have to wait after clicking the button.
             * */
            mAdProvider.loadNewRewardedAd()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY) ?: Bundle().apply {
            putBundle(MAPVIEW_BUNDLE_KEY, this)
        }
        mMapView.onSaveInstanceState(mapViewBundle)
        super.onSaveInstanceState(outState)
    }

    /**
     * Must only be called after we are sure that the given map is initialized
     * */
    private fun addSkycamsToMap(googleMap: GoogleMap, list: List<Skycam>) {
        list.forEach { skycam ->
            val marker = googleMap.addMarker {
                position(
                    LatLng(
                        skycam.location.coordinates.latitude,
                        skycam.location.coordinates.longitude
                    )
                )
            }
            marker.tag = skycam
        }
    }

    override fun onStart() {
        mMapView.onStart()
        super.onStart()
    }

    override fun onResume() {
        mMapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mMapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mMapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mMapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mMapView.onLowMemory()
        super.onLowMemory()
    }

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBoundleKey"
    }

    inner class MapMarkerOnClickListener : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(marker: Marker?): Boolean {
            marker?.let {
                mViewModel.selectMapObject(it.tag)
            }

            // Consume the click event
            return true
        }
    }

    inner class MapClickListener : GoogleMap.OnMapClickListener {
        override fun onMapClick(location: LatLng?) {
            mViewModel.clearMapObjectSelection()
        }
    }
}