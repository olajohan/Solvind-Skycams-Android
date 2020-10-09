package com.lyngennorth.solvindskycams.domain.domainmodel

import com.lyngennorth.solvindskycams.domain.enums.AuroraObservedState

data class SkycamImageUserVote(
    val uid: String,
    val imageName: String,
    val auroraObservedState: AuroraObservedState
)