package com.solvindskycams.skycam.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solvindskycams.app.di.SkycamModuleDependencies
import com.solvindskycams.skycam.R
import com.solvindskycams.skycam.databinding.HomeFragmentBinding
import com.solvindskycams.skycam.di.DaggerSkycamComponent
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var viewmodelFactory: HomeViewModelFactory
    private lateinit var mBinding: HomeFragmentBinding
    private val mSkycamAdapter = SkycamAdapter(arrayListOf())
    private lateinit var mViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        injectFields()
        mBinding = HomeFragmentBinding.inflate(inflater)
        mViewModel = ViewModelProvider(this, viewmodelFactory).get(HomeViewModel::class.java)
        mBinding.lifecycleOwner = viewLifecycleOwner

        mViewModel.skycams.observe(viewLifecycleOwner, { updatedSkycamList ->
            mSkycamAdapter.updateData(updatedSkycamList)
        })

        mViewModel.loadSkycams()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<RecyclerView>(R.id.skycam_recyclerView).apply {
            layoutManager = GridLayoutManager(this@HomeFragment.context, 2)
            adapter = mSkycamAdapter
            setHasFixedSize(true)
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