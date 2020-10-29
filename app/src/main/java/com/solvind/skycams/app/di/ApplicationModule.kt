package com.solvind.skycams.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SkycamImages

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuroraAlarmAppspot

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun providesFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun providesFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Singleton
    @AuroraAlarmAppspot
    @Provides
    fun providesAuroraAlarmAppspotStorage(): FirebaseStorage = Firebase.storage("gs://aurora-alarm.appspot.com")

    @Singleton
    @SkycamImages
    @Provides
    fun providesSkycamImagesStorage(): FirebaseStorage = Firebase.storage("gs://skycam-images")

    @Singleton
    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher() : CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

}