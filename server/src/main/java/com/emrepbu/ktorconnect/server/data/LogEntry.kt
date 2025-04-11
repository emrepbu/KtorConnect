package com.emrepbu.ktorconnect.server.data

import androidx.compose.runtime.Immutable

@Immutable
data class LogEntry(
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
