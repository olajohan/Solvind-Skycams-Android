package com.solvindskycams.skycam.domain.model

import com.solvindskycams.skycam.domain.enums.AuroraObservedState
import com.solvindskycams.skycam.domain.enums.AuroraPredictionLabel

data class SkycamImageDetails(
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