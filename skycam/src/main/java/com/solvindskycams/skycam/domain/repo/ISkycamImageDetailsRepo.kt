package com.solvindskycams.skycam.domain.repo

import com.solvindskycams.common.Either
import com.solvindskycams.common.Failure
import com.solvindskycams.skycam.domain.model.SkycamImageDetails
import com.solvindskycams.skycam.domain.model.SkycamImageDetailsUserVote

interface ISkycamImageDetailsRepo {
    suspend fun getSkycamImage(imageName: String): Either<Failure, SkycamImageDetails>
    suspend fun voteSkycamImage(skycamImageDetailsUserVote: SkycamImageDetailsUserVote): Either<Failure, Unit>
}