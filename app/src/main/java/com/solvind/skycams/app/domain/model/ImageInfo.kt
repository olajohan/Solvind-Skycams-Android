package com.solvind.skycams.app.domain.model

import com.solvind.skycams.app.domain.enums.AuroraPredictionLabel


data class ImageInfo(
    val skycamKey: String,
    val imageId: String,
    val storageLocation: String,
    val timestamp: Long,
    val sunElevation: Double,
    val moonPhase: Double,
    val predictionConfidence: Double,
    val predictionLabel: AuroraPredictionLabel,
)