package com.lyngennorth.solvindskycams.domain.usecases

import com.lyngennorth.solvindskycams.domain.repositories.IUserRepository

class UpdateAlarmAvailableUntilEpochSeconds(private val repo: IUserRepository): UseCase<Unit, UpdateAlarmAvailableUntilEpochSeconds.Params>() {
    data class Params(val plusEpochSeconds: Long)
    override suspend fun run(params: Params) = repo.updateAlarmAvailableUntilEpochSeconds(params.plusEpochSeconds)
}