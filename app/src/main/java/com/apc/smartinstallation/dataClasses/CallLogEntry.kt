package com.apc.smartinstallation.dataClasses


data class CallLogEntry(
    val number: String,
    val type: Int,
    val date: Long,
    val duration: Long
)
