package com.solvind.skycams.app.domain.model

data class SkycamLocation(
    val coordinates: SkycamLocationCoordinates,
    val mas: Int,
    val name: String,
    val region: String,
    val timezone: String
)