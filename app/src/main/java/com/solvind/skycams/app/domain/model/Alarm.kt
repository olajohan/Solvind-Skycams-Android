package com.solvind.skycams.app.domain.model

data class Alarm(
    val skycamKey: String,
    val alarmAvailableUntilEpochSeconds: Long,
    val isActive: Boolean
) {
    override fun equals(other: Any?): Boolean {
        return other is Alarm &&
                other.skycamKey == this.skycamKey &&
                other.alarmAvailableUntilEpochSeconds == this.alarmAvailableUntilEpochSeconds &&
                other.isActive == this.isActive
    }

    override fun hashCode(): Int {
        var result = skycamKey.hashCode()
        result = 31 * result + alarmAvailableUntilEpochSeconds.hashCode()
        result = 31 * result + isActive.hashCode()
        return result
    }
}