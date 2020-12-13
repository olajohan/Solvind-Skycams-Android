package com.solvind.skycams.app.domain.enums

sealed class AuroraPrediction(open val name: String) {
    data class VisibleAurora(override val name: String = "Aurora", val confidence: Double) : AuroraPrediction(name)
    data class NotAurora(override val name: String = "Not aurora", val confidence: Double) : AuroraPrediction(name)
    data class NotPredicted(override val name: String = "Not started") : AuroraPrediction(name)
}