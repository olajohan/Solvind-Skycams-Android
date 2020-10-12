package com.lyngennorth.solvindskycams.skycam.data.repo

import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.skycam.domain.model.SkycamImageDetails
import com.lyngennorth.solvindskycams.skycam.domain.model.SkycamImageDetailsUserVote
import com.lyngennorth.solvindskycams.skycam.domain.repo.ISkycamImageDetailsRepo

class FirestoreSkycamImageDetailsRepoImpl : ISkycamImageDetailsRepo {

    override suspend fun getSkycamImage(imageName: String): Either<Failure, SkycamImageDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun voteSkycamImage(skycamImageDetailsUserVote: SkycamImageDetailsUserVote): Either<Failure, Unit> {
        TODO("Not yet implemented")
    }
}