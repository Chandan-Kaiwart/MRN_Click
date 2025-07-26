package com.apc.smartinstallation.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherTypes {
    val main: CoroutineDispatcher
    val io:CoroutineDispatcher
    val default:CoroutineDispatcher
    val unconfined:CoroutineDispatcher
}