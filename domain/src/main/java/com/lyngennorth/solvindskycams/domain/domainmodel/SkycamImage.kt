package com.lyngennorth.solvindskycams.domain.domainmodel

import com.lyngennorth.solvindskycams.domain.domainmodel.enums.AuroraPredictionLabel
import com.lyngennorth.solvindskycams.domain.enums.AuroraObservedState

data class SkycamImage(
    val skycamKey: String,
    val imageId: String,
    val timestamp: Long,
    val sunElevation: Double,
    val moonPhase: Double,
    val predictionConfidence: Double,
    val predictionLabel: AuroraPredictionLabel,

    // Maps UID to the users AuroraObservedState
    val votes: Map<String, AuroraObservedState>?
)