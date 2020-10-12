package com.lyngennorth.solvindskycams.skycam.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lyngennorth.solvindskycams.app.di.SkycamModuleDependencies
import com.lyngennorth.solvindskycams.skycam.R
import com.lyngennorth.solvindskycams.skycam.databinding.HomeFragmentBinding
import com.lyngennorth.solvindskycams.skycam.di.DaggerSkycamComponent
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var viewmodelFactory: HomeViewModelFactory
    private lateinit var mBinding: HomeFragmentBinding
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        injectFields()
        val viewModel = ViewModelProvider(this, viewmodelFactory).get(HomeViewModel::class.java)
        mBinding = HomeFragmentBinding.inflate(inflater)
        mBinding.lifecycleOwner = viewLifecycleOwner
        return mBinding.root
    }

    private fun injectFields() {
        DaggerSkycamComponent.builder()
            .context(this.requireActivity())
            .appDependencies(
                EntryPointAccessors.fromApplication(
                    requireContext(),
                    SkycamModuleDependencies::class.java
                )).build().inject(this)
    }

}