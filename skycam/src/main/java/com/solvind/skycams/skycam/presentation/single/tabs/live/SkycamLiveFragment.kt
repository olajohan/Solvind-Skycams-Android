package com.solvind.skycams.skycam.presentation.single.tabs.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.solvind.skycams.app.di.SkycamModuleDependencies
import com.solvind.skycams.skycam.databinding.FragmentSkycamLiveBinding
import com.solvind.skycams.skycam.di.DaggerSkycamComponent
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

class SkycamLiveFragment : Fragment() {

    @Inject
    lateinit var mViewModelFactory: SkycamLiveViewModelFactory
    private lateinit var mViewModel: SkycamLiveViewModel
    private lateinit var mBinding: FragmentSkycamLiveBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        injectFields()
        mViewModel = ViewModelProvider(this, mViewModelFactory).get(SkycamLiveViewModel::class.java)
        mBinding = FragmentSkycamLiveBinding.inflate(inflater, container, false)
        mBinding.viewModel = mViewModel
        mBinding.lifecycleOwner = viewLifecycleOwner
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf {
            it.containsKey("skycamKey")
        }?.apply {
            mViewModel.refreshLiveImage(getString("skycamKey").toString())
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