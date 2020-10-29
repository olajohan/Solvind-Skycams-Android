package com.solvind.skycams.app.presentation.single

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.solvind.skycams.app.R
import com.solvind.skycams.app.databinding.FragmentSingleSkycamBinding
import com.solvind.skycams.app.presentation.single.tabs.live.SkycamLiveFragment

private const val NUM_PAGES = 3

class SingleSkycamFragment : Fragment() {

    private lateinit var mViewModel: SingleSkycamViewModel
    private val mArgs: SingleSkycamFragmentArgs by navArgs()
    private lateinit var mBinding: FragmentSingleSkycamBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentSingleSkycamBinding.inflate(inflater, container, false)
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
}