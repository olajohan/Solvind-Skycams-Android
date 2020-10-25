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
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SkycamImages

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuroraAlarmAppspot

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuroraImages

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun providesFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun providesFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @AuroraAlarmAppspot
    @Provides
    fun providesAuroraAlarmAppspotStorage(): FirebaseStorage = Firebase.storage("gs://aurora-alarm.appspot.com")

    @SkycamImages
    @Provides
    fun providesSkycamImagesStorage(): FirebaseStorage = Firebase.storage("gs://skycam-images")

}