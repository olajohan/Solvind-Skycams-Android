package com.lyngennorth.solvindskycams.domain.usecases

import com.lyngennorth.solvindskycams.domain.domainmodel.SkycamImageUserVote
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamImageRepository

class VoteSkycamImage(private val repo: ISkycamImageRepository): UseCase<Unit, VoteSkycamImage.Params>() {
    data class Params(val vote: SkycamImageUserVote)

    override suspend fun run(params: Params) = repo.voteSkycamImage(params.vote)
}