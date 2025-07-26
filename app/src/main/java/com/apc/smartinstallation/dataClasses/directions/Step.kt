package com.apc.smartinstallation.dataClasses.directions

import com.apc.smartinstallation.dataClasses.directions.Distance

data class Step(
    val distance: Distance,
    val duration: Duration,
    val end_location: EndLocation,
    val html_instructions: String,
    val maneuver: String,
    val polyline: Polyline,
    val start_location: StartLocation,
    val travel_mode: String
)