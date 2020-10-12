package com.lyngennorth.solvindskycams.skycam.domain.repo

import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.skycam.domain.model.SkycamImageDetails
import com.lyngennorth.solvindskycams.skycam.domain.model.SkycamImageDetailsUserVote

interface ISkycamImageDetailsRepo {
    suspend fun getSkycamImage(imageName: String): Either<Failure, SkycamImageDetails>
    suspend fun voteSkycamImage(skycamImageDetailsUserVote: SkycamImageDetailsUserVote): Either<Failure, Unit>
}