package com.apc.lossreduction.dataClasses.geocode

data class GeocodeResponse(
    val results: List<Result>,
    val status: String
)