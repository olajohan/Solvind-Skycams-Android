package com.solvindskycams.skycam.data.repo

import com.solvindskycams.common.Either
import com.solvindskycams.common.Failure
import com.solvindskycams.skycam.domain.model.SkycamImageDetails
import com.solvindskycams.skycam.domain.model.SkycamImageDetailsUserVote
import com.solvindskycams.skycam.domain.repo.ISkycamImageDetailsRepo

class FirestoreSkycamImageDetailsRepoImpl : ISkycamImageDetailsRepo {

    override suspend fun getSkycamImage(imageName: String): Either<Failure, SkycamImageDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun voteSkycamImage(skycamImageDetailsUserVote: SkycamImageDetailsUserVote): Either<Failure, Unit> {
        TODO("Not yet implemented")
    }
}