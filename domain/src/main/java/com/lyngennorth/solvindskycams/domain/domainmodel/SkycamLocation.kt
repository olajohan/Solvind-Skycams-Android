package com.lyngennorth.solvindskycams.domain.domainmodel

data class SkycamLocation(
    val coordinates: SkycamLocationCoordinates,
    val metersAboveSeaLevel: Int,
    val name: String,
    val region: String,
    val timezone: String
)