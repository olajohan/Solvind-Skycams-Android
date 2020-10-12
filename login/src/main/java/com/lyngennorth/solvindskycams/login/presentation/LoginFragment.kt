package com.lyngennorth.solvindskycams.login.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.lyngennorth.solvindskycams.app.di.LoginModuleDependencies
import com.lyngennorth.solvindskycams.login.R
import com.lyngennorth.solvindskycams.login.databinding.LoginFragmentBinding
import com.lyngennorth.solvindskycams.login.di.DaggerLoginComponent
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

class LoginFragment : Fragment() {

    @Inject
    lateinit var mViewModelFactory: LoginViewModelFactory

    private lateinit var mBinding: LoginFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        injectFields()
        mBinding = LoginFragmentBinding.inflate(inflater)
        val viewModel = ViewModelProvider(this, mViewModelFactory).get(LoginViewModel::class.java)
        mBinding.viewmodel = viewModel

        viewModel.navigateToHome.observe(viewLifecycleOwner, {
            if (it.getContentIfNotHandled() == null) findNavController().navigate(LoginFragmentDirections.actionLoginToHome())
        })

        return mBinding.root
    }

    private fun injectFields() {
        DaggerLoginComponent.builder()
            .context(this.requireActivity())
            .appDependencies(
                EntryPointAccessors.fromApplication(
                    requireContext(),
                    LoginModuleDependencies::class.java
                )).build().inject(this)
    }
}