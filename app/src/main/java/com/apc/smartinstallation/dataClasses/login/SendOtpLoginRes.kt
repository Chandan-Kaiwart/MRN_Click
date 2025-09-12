package com.apc.smartinstallation.dataClasses.login

data class SendLoginOtpRes(
    val message: String,
    val code: Int,
    val uid: String,
    val id :Int
)