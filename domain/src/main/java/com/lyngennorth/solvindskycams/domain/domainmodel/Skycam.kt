package com.lyngennorth.solvindskycams.domain.domainmodel

data class Skycam(
    val skycamKey: String,
    val mainImage: String,
    val location: SkycamLocation,
    val mostRecentImageName: String
)