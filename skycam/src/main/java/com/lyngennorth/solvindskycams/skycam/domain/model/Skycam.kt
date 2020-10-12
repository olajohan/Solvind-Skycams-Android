package com.lyngennorth.solvindskycams.skycam.domain.model

data class Skycam(
    val skycamKey: String,
    val mainImage: String,
    val location: SkycamLocation,
    val mostRecentImageName: String
)