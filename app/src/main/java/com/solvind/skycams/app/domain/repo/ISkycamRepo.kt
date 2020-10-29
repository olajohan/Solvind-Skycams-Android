package com.solvind.skycams.app.domain.repo

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.Skycam
import kotlinx.coroutines.flow.Flow

interface ISkycamRepo {
    suspend fun getAllSkycams(): Resource<List<Skycam>>
    suspend fun getSkycam(skycamKey: String): Resource<Skycam>
    fun getSkycamFlow(skycamKey: String): Flow<Skycam>
}