package com.lyngennorth.solvindskycams.skycam.domain.model

import com.lyngennorth.solvindskycams.skycam.domain.enums.AuroraObservedState

data class SkycamImageDetailsUserVote(
    val uid: String,
    val imageName: String,
    val auroraObservedState: AuroraObservedState
)