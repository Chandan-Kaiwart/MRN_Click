package com.apc.smartinstallation.dataClasses

data class User(
    val agency: Int,
    val cur_loc: String,
    val email: String,
    val id: Int,
    val mob: String,
    val mru: Int,
    val name: String,
    val status: Int
)