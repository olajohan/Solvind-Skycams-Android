package com.solvind.skycams.app.di

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class FirebaseStorageModule {
    @Singleton
    @AuroraAlarmAppspot
    @Provides
    fun providesAuroraAlarmAppspotStorage(): FirebaseStorage = Firebase.storage("gs://aurora-alarm.appspot.com")

    @Singleton
    @SkycamImages
    @Provides
    fun providesSkycamImagesStorage(): FirebaseStorage = Firebase.storage("gs://skycam-images")
}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SkycamImages

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuroraAlarmAppspot

