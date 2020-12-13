package com.solvind.skycams.app.domain.model

data class Skycam(
    val skycamKey: String,
    val mainImage: String,
    val location: SkycamLocation,
    val mostRecentImage: ImageInfo
)