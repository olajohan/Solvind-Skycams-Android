package com.solvind.skycams.skycam.presentation.single

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.solvind.skycams.app.di.SkycamModuleDependencies
import com.solvind.skycams.skycam.R
import com.solvind.skycams.skycam.databinding.SingleSkycamFragmentBinding
import com.solvind.skycams.skycam.di.DaggerSkycamComponent
import com.solvind.skycams.skycam.presentation.single.tabs.live.SkycamLiveFragment
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

private const val NUM_PAGES = 3

class SingleSkycamFragment : Fragment() {

    @Inject
    lateinit var mSingleSkycamViewModelFactory: SingleSkycamViewModelFactory

    private lateinit var mViewModel: SingleSkycamViewModel
    private val mArgs: SingleSkycamFragmentArgs by navArgs()
    private lateinit var mBinding: SingleSkycamFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        injectFields()
        mViewModel = ViewModelProvider(this, mSingleSkycamViewModelFactory).get(SingleSkycamViewModel::class.java)
        mBinding = SingleSkycamFragmentBinding.inflate(inflater, container, false)
        mBinding.skycamMainImage = mArgs.skycamMainImage
        mBinding.skycamName = mArgs.skycamName

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        viewPager.adapter = ScreenSlidePagerAdapter(requireActivity())
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "Live"
                1 -> "History"
                else -> "Forecast"
            }
        }.attach()
        view.findViewById<Toolbar>(R.id.toolbar).setupWithNavController(view.findNavController())
    }

    private inner class ScreenSlidePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int  = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            val fragment = SkycamLiveFragment()
            fragment.arguments = Bundle().apply {
                putString("skycamKey", mArgs.skycamKey)
            }
            return fragment
        }
    }

    private fun injectFields() {
        DaggerSkycamComponent.builder()
            .context(this.requireContext())
            .appDependencies(
                EntryPointAccessors.fromApplication(
                    requireContext(),
                    SkycamModuleDependencies::class.java
                )
            ).build().inject(this)
    }
}