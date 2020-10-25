package com.solvind.skycams.skycam.domain.repo

import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.domain.model.Skycam
import kotlinx.coroutines.flow.Flow

interface ISkycamRepo {
    suspend fun getAllSkycams(): Result<List<Skycam>>
    suspend fun getSkycam(skycamKey: String): Result<Skycam>
    fun getSkycamFlow(skycamKey: String): Flow<Skycam>
}