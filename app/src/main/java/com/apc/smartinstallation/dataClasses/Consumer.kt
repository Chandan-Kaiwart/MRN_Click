package com.apc.smartinstallation.dataClasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Consumer(
    val acct_id: String,
    val address: String,
    val createdAt: String,
    val id: String,
    val landmark: String,
    val latitude: String,
    val longitude: String,
    val meter_reader_id: String,
    val mobile: String,
    val name: String,
    val reading_id: String,
    val status: String,
    val udpatedAt: String
):Parcelable