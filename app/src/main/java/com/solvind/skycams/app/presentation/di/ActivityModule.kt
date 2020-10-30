package com.solvind.skycams.app.presentation.di

import com.solvind.skycams.app.presentation.ads.AdsProviderImpl
import com.solvind.skycams.app.presentation.ads.IAdsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class ActivityModule {

    @ActivityScoped
    @Binds
    abstract fun bindsAdsProvider(adsProviderImpl: AdsProviderImpl) : IAdsProvider

}