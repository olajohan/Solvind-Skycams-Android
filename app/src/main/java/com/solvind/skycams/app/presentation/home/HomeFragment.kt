package com.solvind.skycams.app.presentation.home

import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import com.solvind.skycams.app.R
import com.solvind.skycams.app.databinding.FragmentHomeBinding
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


/**
 * Presents all the skycams a list to the user. When the user clicks one of the skycams it should
 * take the user to single skycam fragment
 */
@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment(
    private val mMapMarkerMarkerHandler: MapMarkerHandler
) : Fragment() {

    private val mViewModel: HomeViewModel by viewModels()
    private lateinit var mMapView: MapView
    private lateinit var mBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentHomeBinding.inflate(inflater)
        mBinding.viewModel = mViewModel
        mBinding.lifecycleOwner = viewLifecycleOwner

        return mBinding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        mMapView = requireActivity().findViewById<MapView>(R.id.mapView)
        mMapView.onCreate(mapViewBundle)
        lifecycleScope.launchWhenCreated {

            /*
             * The google map will be safe to use after the call to awaitmap.
             * */
            val googleMap = mMapView.awaitMap()

            /*
            * Set camera to be centered at Lyngen North upon startup
            * */
            googleMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        LatLng(69.76267394506772, 20.467046486726474),
                        5f,
                        0f,
                        0f
                    )
                )
            )

            googleMap.setOnMarkerClickListener {
                when (val tag = it.tag) {
                    is Skycam -> {
                        mViewModel.selectSkycam(tag)

                        /*
                        * Set the initial state of the fab to activate alarm in case the user
                        * has never activated the alarm before.
                        * */
                        mBinding.fab.apply {
                            setOnClickListener { mViewModel.activateAlarm(tag.skycamKey) }
                            setImageIcon(
                                Icon.createWithResource(
                                    requireContext(),
                                    R.drawable.ic_baseline_alarm_add_24
                                )
                            )
                            show()
                        }
                    }
                }
                return@setOnMarkerClickListener true
            }

            googleMap.setOnMapClickListener {
                mViewModel.hideBottomSheet()
                mBinding.fab.visibility = View.GONE
            }

            mViewModel.skycamList.observe(viewLifecycleOwner, {
                mMapMarkerMarkerHandler.addSkycamMarkers(googleMap, it)
            })

            mViewModel.fabState.observe(viewLifecycleOwner) { fabState ->
                when (fabState) {
                    FabState.Hidden -> mBinding.fab.hide()
                    is FabState.AlarmActivated -> {
                        mBinding.fab.apply {
                            setOnClickListener { mViewModel.deactivateAlarm(fabState.alarmConfig.skycamKey) }
                            setImageIcon(
                                Icon.createWithResource(
                                    requireContext(),
                                    R.drawable.ic_baseline_alarm_on_24
                                )
                            )
                        }
                    }
                    is FabState.AlarmDeactivated -> {
                        mBinding.fab.apply {
                            setOnClickListener { mViewModel.activateAlarm(fabState.alarmConfig.skycamKey) }
                            setImageIcon(
                                Icon.createWithResource(
                                    requireContext(),
                                    R.drawable.ic_baseline_alarm_add_24
                                )
                            )
                        }
                    }
                    is FabState.AlarmTimedOut -> {
                        mBinding.fab.apply {
                            this.tag = fabState.alarmConfig.skycamKey
                            setOnClickListener {
                                (requireActivity() as MainActivity).onClickWatchAdForReward(this)
                            }
                            setImageIcon(
                                Icon.createWithResource(
                                    requireContext(),
                                    R.drawable.ic_baseline_more_time_24
                                )
                            )
                        }
                    }

                }
            }

            /*
             * Consume the click, so it will not get passed down to the map which hides the bottom sheet
             * when it receives a click
             * */
            mBinding.bottomSheet.setOnClickListener {}
        }
    }

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBoundleKey"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY) ?: Bundle().apply {
            putBundle(MAPVIEW_BUNDLE_KEY, this)
        }
        mMapView.onSaveInstanceState(mapViewBundle)
        super.onSaveInstanceState(outState)
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
}