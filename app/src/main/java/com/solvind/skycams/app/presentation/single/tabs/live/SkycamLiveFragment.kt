package com.solvind.skycams.app.presentation.single.tabs.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.solvind.skycams.app.databinding.FragmentSkycamLiveBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkycamLiveFragment : Fragment() {

    private val mViewModel: SkycamLiveViewModel by viewModels()
    private lateinit var mBinding: FragmentSkycamLiveBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentSkycamLiveBinding.inflate(inflater, container, false)
        mBinding.viewModel = mViewModel
        mBinding.lifecycleOwner = viewLifecycleOwner
        return mBinding.root
    }
}