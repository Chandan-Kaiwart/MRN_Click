package com.apc.smartinstallation.dataClasses.login

data class LoginRes(
    val error: Boolean,
    val message: String,
    val permissions: List<String>,
    val user_role: String,
    val username: String
)