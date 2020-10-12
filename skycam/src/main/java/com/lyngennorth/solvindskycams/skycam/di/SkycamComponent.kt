package com.lyngennorth.solvindskycams.skycam.di

import android.content.Context
import com.lyngennorth.solvindskycams.app.di.SkycamModuleDependencies
import com.lyngennorth.solvindskycams.skycam.data.di.DataModule
import com.lyngennorth.solvindskycams.skycam.presentation.di.PresentationModule
import com.lyngennorth.solvindskycams.skycam.presentation.list.HomeFragment
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [SkycamModuleDependencies::class],
    modules = [
        DataModule::class,
        PresentationModule::class
    ]
)
interface SkycamComponent {
    fun inject(homeFragment: HomeFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun appDependencies(skycamModuleDependencies: SkycamModuleDependencies): Builder
        fun build(): SkycamComponent
    }

}