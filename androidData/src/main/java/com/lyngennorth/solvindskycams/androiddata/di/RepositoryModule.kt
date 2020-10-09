package com.lyngennorth.solvindskycams.androiddata.di

import com.lyngennorth.solvindskycams.androiddata.repositories.FirestoreSkycamImageRepositoryImpl
import com.lyngennorth.solvindskycams.androiddata.repositories.FirestoreSkycamRepositoryImpl
import com.lyngennorth.solvindskycams.androiddata.repositories.FirestoreUserRepositoryImpl
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamImageRepository
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamRepository
import com.lyngennorth.solvindskycams.domain.repositories.IUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindFirestoreSkycamRepository(repo: FirestoreSkycamRepositoryImpl): ISkycamRepository

    @Singleton
    @Binds
    abstract fun bindFirestoreSkycamImagerepository(repo: FirestoreSkycamImageRepositoryImpl): ISkycamImageRepository

    @Singleton
    @Binds
    abstract fun bindFirestoreUserRepository(repo: FirestoreUserRepositoryImpl): IUserRepository
}