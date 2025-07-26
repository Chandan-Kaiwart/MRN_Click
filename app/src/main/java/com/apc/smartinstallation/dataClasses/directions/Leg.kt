package com.apc.smartinstallation.dataClasses.directions

import com.apc.smartinstallation.dataClasses.directions.Duration
import com.apc.smartinstallation.dataClasses.directions.EndLocation
import com.apc.smartinstallation.dataClasses.directions.StartLocation
import com.apc.smartinstallation.dataClasses.directions.Step

data class Leg(
    val distance: Distance,
    val duration: Duration,
    val end_address: String,
    val end_location: EndLocation,
    val start_address: String,
    val start_location: StartLocation,
    val steps: List<Step>,
    val traffic_speed_entry: List<Any>,
    val via_waypoint: List<Any>
)