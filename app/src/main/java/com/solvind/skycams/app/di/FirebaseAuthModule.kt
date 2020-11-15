package com.solvind.skycams.app.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class FirebaseAuthModule {

    @Singleton
    @Provides
    fun providesFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

}