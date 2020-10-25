package com.solvind.skycams.skycam.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.solvind.skycams.app.di.SkycamModuleDependencies
import com.solvind.skycams.skycam.R
import com.solvind.skycams.skycam.databinding.HomeFragmentBinding
import com.solvind.skycams.skycam.di.DaggerSkycamComponent
import com.solvind.skycams.skycam.domain.model.Skycam
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject


/**
 * Presents all the skycams in grid to the user. When the user clicks one of the skycams it should
 * take the user to single skycam fragment
 */
class HomeFragment : Fragment() {

    @Inject
    lateinit var viewmodelFactory: HomeViewModelFactory
    private lateinit var mBinding: HomeFragmentBinding
    private val mSkycamAdapter = SkycamAdapter(arrayListOf(), this)
    private lateinit var mViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        injectFields()

        mBinding = HomeFragmentBinding.inflate(inflater, container, false)
        mViewModel = ViewModelProvider(this, viewmodelFactory).get(HomeViewModel::class.java)

        mViewModel.skycams.observe(viewLifecycleOwner, { updatedSkycamList ->
            mSkycamAdapter.replaceDatasetAndNotify(updatedSkycamList)
        })

        mBinding.skycamRecyclerView.apply {
            layoutManager = GridLayoutManager(this@HomeFragment.context, 2)
            adapter = mSkycamAdapter
            setHasFixedSize(true)
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfigureation = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.toolbar).setupWithNavController(navController, appBarConfigureation)
    }

    override fun onStart() {
        super.onStart()
    }

    fun navigateToSingleSkycam(skycam: Skycam) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSingleSkycamFragment(
            skycam.skycamKey,
            skycam.location.name,
            skycam.mainImage)
        )
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