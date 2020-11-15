package com.solvind.skycams.app.data.di

import com.solvind.skycams.app.data.repo.FirestoreAlarmConfigRepoImpl
import com.solvind.skycams.app.data.repo.FirestoreImageInfoRepoImpl
import com.solvind.skycams.app.data.repo.FirestoreSkycamRepoImpl
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import com.solvind.skycams.app.domain.repo.IImageInfoRepo
import com.solvind.skycams.app.domain.repo.ISkycamRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
abstract class DataModule {

    @Singleton
    @Binds
    abstract fun bindsFirestoreSkycamRepo(firestoreSkycamRepoImpl: FirestoreSkycamRepoImpl) : ISkycamRepo

    @Singleton
    @Binds
    abstract fun bindsFirestoreImageInfoRepo(firestoreImageInfoRepoImpl: FirestoreImageInfoRepoImpl) : IImageInfoRepo

    @Singleton
    @Binds
    abstract fun bindsFirestoreAlarmRepo(firestoreAlarmRepoImpl: FirestoreAlarmConfigRepoImpl) : IAlarmConfigRepo

}