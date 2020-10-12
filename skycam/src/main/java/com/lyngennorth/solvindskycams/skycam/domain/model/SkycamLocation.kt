package com.lyngennorth.solvindskycams.skycam.domain.model

data class SkycamLocation(
    val coordinates: SkycamLocationCoordinates,
    val metersAboveSeaLevel: Int,
    val name: String,
    val region: String,
    val timezone: String
)