package com.lyngennorth.solvindskycams.auth.domain.repo

import com.lyngennorth.solvindskycams.auth.domain.model.AccountData
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure

interface IUserAccountDataRepo {

    suspend fun getUserAccountData(uid: String): Either<Failure, AccountData>
}