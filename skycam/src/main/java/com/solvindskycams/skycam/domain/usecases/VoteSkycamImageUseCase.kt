package com.solvindskycams.skycam.domain.usecases

import com.solvindskycams.common.usecases.UseCase
import com.solvindskycams.skycam.domain.model.SkycamImageDetailsUserVote
import com.solvindskycams.skycam.domain.repo.ISkycamImageDetailsRepo

class VoteSkycamImageUseCase(private val detailsRepo: ISkycamImageDetailsRepo): UseCase<Unit, VoteSkycamImageUseCase.Params>() {
    data class Params(val voteDetails: SkycamImageDetailsUserVote)

    override suspend fun run(params: Params) = detailsRepo.voteSkycamImage(params.voteDetails)
}