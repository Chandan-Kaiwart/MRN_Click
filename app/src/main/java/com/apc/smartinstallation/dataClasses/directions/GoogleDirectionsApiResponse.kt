package com.apc.smartinstallation.dataClasses.directions

data class GoogleDirectionsApiResponse(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)