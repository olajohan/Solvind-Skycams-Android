package com.solvindskycams.skycam.domain.model

data class Skycam(
    val skycamKey: String,
    val mainImage: String,
    val location: SkycamLocation,
    val mostRecentImageId: String
) {
    override fun equals(other: Any?): Boolean = if (other is Skycam) other.skycamKey == skycamKey else false

}