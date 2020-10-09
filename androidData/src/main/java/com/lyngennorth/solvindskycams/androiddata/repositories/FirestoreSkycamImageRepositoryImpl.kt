package com.lyngennorth.solvindskycams.androiddata.repositories

import com.lyngennorth.solvindskycams.domain.Either
import com.lyngennorth.solvindskycams.domain.Failure
import com.lyngennorth.solvindskycams.domain.domainmodel.SkycamImage
import com.lyngennorth.solvindskycams.domain.domainmodel.SkycamImageUserVote
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamImageRepository

class FirestoreSkycamImageRepositoryImpl : ISkycamImageRepository {

    override suspend fun getSkycamImage(imageName: String): Either<Failure, SkycamImage> {
        TODO("Not yet implemented")
    }

    override suspend fun voteSkycamImage(skycamImageUserVote: SkycamImageUserVote): Either<Failure, Unit> {
        TODO("Not yet implemented")
    }
}