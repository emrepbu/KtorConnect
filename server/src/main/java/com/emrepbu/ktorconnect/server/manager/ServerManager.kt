package com.emrepbu.ktorconnect.server.manager

import android.content.Context
import com.emrepbu.ktorconnect.server.data.LogEntry
import com.emrepbu.ktorconnect.server.data.ServerAddress
import com.emrepbu.ktorconnect.server.data.ServerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ServerManager(
    private val context: Context,
) {
    private val _serverStatus = MutableStateFlow(ServerStatus.STOPPED)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    private val _serverAddresses = MutableStateFlow<List<ServerAddress>>(emptyList())
    val serverAddresses: StateFlow<List<ServerAddress>> = _serverAddresses.asStateFlow()

    private val _serverPort = MutableStateFlow(8080)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun startServer(port: Int) {
        if (_serverStatus.value == ServerStatus.RUNNING ||
            _serverStatus.value == ServerStatus.STARTING
        ) {
            addLog("Server is already running or starting")
        }

        _serverStatus.value = ServerStatus.STARTING
        _serverPort.value = port
        addLog("Starting server on port $port")

        // TODO: Create service
//        val intent = Intent(context, ServerService::class.java)
//        context.startService(intent)
    }

    fun stopServer() {
        if (_serverStatus.value == ServerStatus.STOPPED ||
            _serverStatus.value == ServerStatus.STOPPING
        ) {
            addLog("Server is already stopped")
            return
        }
        // ...
    }

    fun addLog(message: String, isError: Boolean = false) {
        val newLog = LogEntry(message, isError)
        _logs.value += newLog
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}