package com.solvindskycams.skycam.data.di


import com.solvindskycams.skycam.data.repo.FirestoreSkycamImageDetailsRepoImpl
import com.solvindskycams.skycam.data.repo.FirestoreSkycamRepoImpl
import com.solvindskycams.skycam.domain.repo.ISkycamImageDetailsRepo
import com.solvindskycams.skycam.domain.repo.ISkycamRepo
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class DataModule {

    @Singleton
    @Binds
    abstract fun bindFirestoreSkycamRepository(repo: FirestoreSkycamRepoImpl): ISkycamRepo

    @Singleton
    @Binds
    abstract fun bindFirestoreSkycamImagerepository(repo: FirestoreSkycamImageDetailsRepoImpl): ISkycamImageDetailsRepo
}