package com.lyngennorth.solvindskycams.skycam.domain.usecases

import com.lyngennorth.solvindskycams.common.usecases.UseCase
import com.lyngennorth.solvindskycams.skycam.domain.model.SkycamImageDetailsUserVote
import com.lyngennorth.solvindskycams.skycam.domain.repo.ISkycamImageDetailsRepo

class VoteSkycamImage(private val detailsRepo: ISkycamImageDetailsRepo): UseCase<Unit, VoteSkycamImage.Params>() {
    data class Params(val voteDetails: SkycamImageDetailsUserVote)

    override suspend fun run(params: Params) = detailsRepo.voteSkycamImage(params.voteDetails)
}