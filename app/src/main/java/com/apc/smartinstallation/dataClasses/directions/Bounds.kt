package com.apc.smartinstallation.dataClasses.directions

import com.apc.smartinstallation.dataClasses.directions.Northeast
import com.apc.smartinstallation.dataClasses.directions.Southwest

data class Bounds(
    val northeast: Northeast,
    val southwest: Southwest
)