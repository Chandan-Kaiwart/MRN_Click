package com.apc.smartinstallation.dataClasses.login

import com.apc.smartinstallation.dataClasses.User


data class VerLoginOtpRes(
    val access_token: String,
    val user: User
)