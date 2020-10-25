package com.solvind.skycams.skycam.di

import android.content.Context
import com.solvind.skycams.app.di.SkycamModuleDependencies
import com.solvind.skycams.skycam.data.di.DataModule
import com.solvind.skycams.skycam.presentation.di.PresentationModule
import com.solvind.skycams.skycam.presentation.list.HomeFragment
import com.solvind.skycams.skycam.presentation.single.SingleSkycamFragment
import com.solvind.skycams.skycam.presentation.single.tabs.live.SkycamLiveFragment
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
    fun inject(singleSkycamFragment: SingleSkycamFragment)
    fun inject(skycamLiveFragment: SkycamLiveFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun appDependencies(skycamModuleDependencies: SkycamModuleDependencies): Builder
        fun build(): SkycamComponent
    }

}