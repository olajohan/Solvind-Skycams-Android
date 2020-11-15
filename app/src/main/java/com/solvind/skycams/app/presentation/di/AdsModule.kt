package com.solvind.skycams.app.presentation.di

import android.content.Context
import com.google.android.ump.UserMessagingPlatform
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Module
@InstallIn(ActivityComponent::class)
abstract class AdsBindsModule {}

@Module
@InstallIn(ActivityComponent::class)
object AdsProvidesModule {

    @ActivityScoped
    @Provides
    fun provideConsentInfo(@ActivityContext context: Context) = UserMessagingPlatform.getConsentInformation(context)

}