package com.lyngennorth.solvindskycams.domain.repositories

import com.lyngennorth.solvindskycams.domain.Either
import com.lyngennorth.solvindskycams.domain.Failure
import com.lyngennorth.solvindskycams.domain.domainmodel.SkycamImage
import com.lyngennorth.solvindskycams.domain.domainmodel.SkycamImageUserVote

interface ISkycamImageRepository {
    suspend fun getSkycamImage(imageName: String): Either<Failure, SkycamImage>
    suspend fun voteSkycamImage(skycamImageUserVote: SkycamImageUserVote): Either<Failure, Unit>
}