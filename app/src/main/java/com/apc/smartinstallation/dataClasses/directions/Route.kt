package com.apc.smartinstallation.dataClasses.directions

import com.apc.smartinstallation.dataClasses.directions.Bounds
import com.apc.smartinstallation.dataClasses.directions.Leg

data class Route(
    val bounds: Bounds,
    val copyrights: String,
    val legs: List<Leg>,
    val overview_polyline: OverviewPolyline,
    val summary: String,
    val warnings: List<Any>,
    val waypoint_order: List<Int>
)