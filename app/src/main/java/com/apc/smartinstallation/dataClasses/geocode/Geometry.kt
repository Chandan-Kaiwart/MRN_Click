package com.apc.lossreduction.dataClasses.geocode

data class Geometry(
    val bounds: Bounds,
    val location: Location,
    val location_type: String,
    val viewport: Viewport
)