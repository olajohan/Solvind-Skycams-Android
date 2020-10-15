package com.solvindskycams.skycam.di

import android.content.Context
import com.solvindskycams.app.di.SkycamModuleDependencies
import com.solvindskycams.skycam.data.di.DataModule
import com.solvindskycams.skycam.presentation.di.PresentationModule
import com.solvindskycams.skycam.presentation.list.HomeFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
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