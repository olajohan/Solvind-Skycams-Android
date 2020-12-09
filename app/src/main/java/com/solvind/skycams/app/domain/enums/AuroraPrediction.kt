package com.solvind.skycams.app.domain.enums

sealed class AuroraPrediction(open val name: String) {
    data class VisibleAurora(override val name: String = "Aurora is visible", val confidence: Double) : AuroraPrediction(name)
    data class NotAurora(override val name: String = "Aurora is not visible", val confidence: Double) : AuroraPrediction(name)
    data class NotPredicted(override val name: String = "Predictions Unavailable") : AuroraPrediction(name)
}