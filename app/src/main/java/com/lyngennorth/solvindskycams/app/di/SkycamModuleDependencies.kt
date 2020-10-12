package com.lyngennorth.solvindskycams.app.di

import com.lyngennorth.solvindskycams.auth.domain.repo.IAuthenticatorRepo
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@EntryPoint
@InstallIn(ApplicationComponent::class)
interface SkycamModuleDependencies {

    fun bindsAuthenticatorRepo(): IAuthenticatorRepo
}