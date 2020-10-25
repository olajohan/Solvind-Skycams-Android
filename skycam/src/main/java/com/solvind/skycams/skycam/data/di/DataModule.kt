package com.solvind.skycams.skycam.data.di



import com.solvind.skycams.skycam.data.repo.FirestoreAlarmRepoImpl
import com.solvind.skycams.skycam.data.repo.FirestoreImageInfoRepoImpl
import com.solvind.skycams.skycam.data.repo.FirestoreSkycamRepoImpl
import com.solvind.skycams.skycam.domain.repo.IAlarmRepo
import com.solvind.skycams.skycam.domain.repo.IImageInfoRepo
import com.solvind.skycams.skycam.domain.repo.ISkycamRepo
import dagger.Binds
import dagger.Module
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
abstract class DataModule {

    @Singleton
    @Binds
    abstract fun bindSkycamRepository(repoFirestore: FirestoreSkycamRepoImpl): ISkycamRepo

    @Singleton
    @Binds
    abstract fun bindFirestoreImagerepository(repoFirestore: FirestoreImageInfoRepoImpl): IImageInfoRepo

    @Singleton
    @Binds
    abstract fun bindFirestoreAlarmRepository(rep: FirestoreAlarmRepoImpl): IAlarmRepo
}