package com.solvind.skycams.app.domain.model

data class Skycam(
    val skycamKey: String,
    val mainImage: String,
    val location: SkycamLocation,
    val mostRecentImage: ImageInfo
) {
    override fun equals(other: Any?): Boolean = if (other is Skycam) other.skycamKey == skycamKey else false
    override fun hashCode(): Int {
        var result = skycamKey.hashCode()
        result = 31 * result + mainImage.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + mostRecentImage.hashCode()
        return result
    }

}