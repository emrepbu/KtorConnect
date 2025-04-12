package com.emrepbu.ktorconnect.server.ktorserver

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object BroadcastChannel {
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    suspend fun broadcast(message: String) {
        _messages.emit(message)
    }
}