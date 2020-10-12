package com.lyngennorth.solvindskycams.login.di

import android.content.Context
import com.lyngennorth.solvindskycams.app.di.LoginModuleDependencies
import com.lyngennorth.solvindskycams.login.presentation.LoginFragment
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [LoginModuleDependencies::class],
    modules = []
)
interface LoginComponent {
    fun inject(homeFragment: LoginFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun appDependencies(loginModuleDependencies: LoginModuleDependencies): Builder
        fun build(): LoginComponent
    }

}