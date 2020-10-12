package com.lyngennorth.solvindskycams.auth.domain.usecases

import com.lyngennorth.solvindskycams.auth.domain.model.AccountData
import com.lyngennorth.solvindskycams.auth.domain.repo.IUserAccountDataRepo
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.common.usecases.UseCase
import javax.inject.Inject

class GetUserAccountData @Inject constructor(private val dataRepo: IUserAccountDataRepo) : UseCase<AccountData, GetUserAccountData.Params>() {

    data class Params(val uid: String)

    override suspend fun run(params: Params): Either<Failure, AccountData> = dataRepo.getUserAccountData(params.uid)
}