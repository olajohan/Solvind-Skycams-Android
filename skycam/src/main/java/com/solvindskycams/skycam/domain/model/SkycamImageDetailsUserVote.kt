package com.solvindskycams.skycam.domain.model

import com.solvindskycams.skycam.domain.enums.AuroraObservedState

data class SkycamImageDetailsUserVote(
    val uid: String,
    val imageName: String,
    val auroraObservedState: AuroraObservedState
)