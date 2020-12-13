package com.solvind.skycams.app.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.maps.android.ktx.awaitMap
import com.solvind.skycams.app.R
import com.solvind.skycams.app.databinding.FragmentHomeBinding
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import java.util.concurrent.TimeUnit


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
                    }
                }
                return@setOnMarkerClickListener true
            }

            googleMap.setOnMapClickListener {
                mViewModel.hideBottomSheet()
            }

            mViewModel.skycamList.observe(viewLifecycleOwner) {
                mMapMarkerMarkerHandler.addSkycamMarkers(googleMap, it)
            }

            mViewModel.selectedSkycam.observe(viewLifecycleOwner) { skycamOrNull ->
                mBinding.activateAlarmSwitch.setOnClickListener { switch ->
                    switch as SwitchMaterial
                    skycamOrNull?.let { skycam ->
                        if (switch.isChecked) {
                            mViewModel.activateAlarm(skycam.skycamKey)
                        } else {
                            mViewModel.deactivateAlarm(skycam.skycamKey)
                        }
                    }
                }

                skycamOrNull?.let {
                    mBinding.thresholdSeekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            mBinding.thresholdPercentTextView.text = "$progress%"
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            seekBar?.progress?.let { progress ->
                                mViewModel.setNewAlarmConfigThreshold(skycamOrNull.skycamKey, progress)
                            }
                        }
                    })
                } ?: mBinding.thresholdSeekbar.setOnSeekBarChangeListener(null)
            }

            mViewModel.selectedAlarmConfig.observe(viewLifecycleOwner) { alarmConfig ->
                mBinding.thresholdSeekbar.progress = alarmConfig.threshold
                mBinding.thresholdPercentTextView.text = "${alarmConfig.threshold}%"
            }

            mViewModel.showFirstTimeActivationDialog.observe(viewLifecycleOwner) { singleEvent ->
                singleEvent.getContentIfNotHandled()?.let { skycamKey ->
                    showFirstTimeActivatedAlarmDialog(skycamKey)
                }
            }

            mViewModel.showAlarmHasTimedOutWhenActivatingDialog.observe(viewLifecycleOwner) { singleEvent ->
                singleEvent.getContentIfNotHandled()?.let { alarmConfig ->
                    showAlarmHasTimedOutWhenActivatingDialog(alarmConfig)
                }
            }

            /*
             * Consume the click, so it will not get passed down to the map which hides the bottom sheet
             * when it receives a click
             * */
            mBinding.bottomSheet.setOnClickListener {}
        }
    }

    private fun showAlarmHasTimedOutWhenActivatingDialog(alarmConfig: AlarmConfig) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alarm has timed out")
            .setMessage("The alarm you have activated has timed out. Click the '+30 min' button to watch an ad and gain more minutes.")
            .setPositiveButton("+30 min") { dialog, which ->
                (requireActivity() as MainActivity).watchAdForReward(alarmConfig.skycamKey)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                Timber.i("Dismissed watch ad suggestion")
            }
            .show()

    }

    private fun showFirstTimeActivatedAlarmDialog(skycamKey: String) {
        mViewModel.rewardUserAlarmTimeFirstActivation(skycamKey, TimeUnit.HOURS.toSeconds(1L))

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Congratulations!")
            .setMessage("This is the first time you are activating the alarm for this skycam. You have receive 1h of free alarm time.")
            .setPositiveButton("OK"
            ) { dialog, which -> }.show()
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