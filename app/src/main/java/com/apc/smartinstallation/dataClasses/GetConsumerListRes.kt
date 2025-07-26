package com.apc.smartinstallation.dataClasses

data class GetConsumerListRes(
    val data: List<Consumer>,
    val error: Boolean,
    val message: String
)