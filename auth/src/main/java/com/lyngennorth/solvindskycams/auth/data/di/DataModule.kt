package com.lyngennorth.solvindskycams.auth.data.di

import com.google.firebase.auth.FirebaseAuth
import com.lyngennorth.solvindskycams.auth.data.FirebaseAuthenticatorRepoImpl
import com.lyngennorth.solvindskycams.auth.domain.repo.IAuthenticatorRepo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
abstract class DataModule {

    @Singleton
    @Binds
    abstract fun bindFirestoreAuthenticator(firebaseAuthenticatorImpl: FirebaseAuthenticatorRepoImpl): IAuthenticatorRepo
}