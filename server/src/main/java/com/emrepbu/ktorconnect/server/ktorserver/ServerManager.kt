package com.emrepbu.ktorconnect.server.ktorserver

import android.content.Context
import android.content.Intent
import com.emrepbu.ktorconnect.server.data.LogEntry
import com.emrepbu.ktorconnect.server.data.ServerAddress
import com.emrepbu.ktorconnect.server.data.ServerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.NetworkInterface

class ServerManager(private val context: Context) {
    private val _serverStatus = MutableStateFlow(ServerStatus.STOPPED)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    private val _serverPort = MutableStateFlow(8080)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _serverAddresses = MutableStateFlow<List<ServerAddress>>(emptyList())
    val serverAddresses: StateFlow<List<ServerAddress>> = _serverAddresses.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun startServer(port: Int = 8080) {
        if (_serverStatus.value == ServerStatus.RUNNING || _serverStatus.value == ServerStatus.STARTING) {
            addLog("Server already running")
            return
        }

        _serverStatus.value = ServerStatus.STARTING
        _serverPort.value = port
        addLog("Server starting. (Port: $port)...")

        val intent = Intent(context, ServerService::class.java).apply {
            action = ServerService.ACTION_START
            putExtra(ServerService.EXTRA_PORT, port)
        }
        context.startService(intent)
    }

    fun stopServer() {
        if (_serverStatus.value == ServerStatus.STOPPED || _serverStatus.value == ServerStatus.STOPPING) {
            addLog("Server already stopped.")
            return
        }

        _serverStatus.value = ServerStatus.STOPPING
        addLog("Server stopping...")

        val intent = Intent(context, ServerService::class.java).apply {
            action = ServerService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun updateServerStatus(status: ServerStatus) {
        _serverStatus.value = status

        when (status) {
            ServerStatus.RUNNING -> {
                addLog("Server running. (Port: ${_serverPort.value})")
                logLocalIpAddress()
            }

            ServerStatus.STOPPED -> {
                addLog("Server stopped.")
                _serverAddresses.value = emptyList()
            }

            ServerStatus.ERROR -> {
                addLog("Server error.", true)
                _serverAddresses.value = emptyList()
            }

            else -> {
                /* Do nothing */
            }
        }
    }

    fun addLog(message: String, isError: Boolean = false) {
        val newLog = LogEntry(message, isError)
        _logs.value = _logs.value + newLog
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private fun logLocalIpAddress() {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()
            val addresses = mutableListOf<ServerAddress>()

            /**
             * Always add localhost because it is a broadcast address
             */
            addresses.add(ServerAddress.LOCALHOST)

            networkInterfaces.forEach { networkInterface ->
                if (!networkInterface.isUp || networkInterface.isLoopback) {
                    return@forEach
                }

                networkInterface.inetAddresses.toList().forEach { address ->
                    if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') < 0) {
                        val ipAddress = address.hostAddress
                        addresses.add(ServerAddress(ipAddress))
                        addLog("Server IP Address: $ipAddress:${_serverPort.value}")
                    }
                }
            }

            _serverAddresses.value = addresses

        } catch (e: Exception) {
            addLog("Error getting local IP address: ${e.message}", true)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ServerManager? = null

        fun getInstance(context: Context): ServerManager {
            return INSTANCE ?: synchronized(this) {
                val instance = ServerManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}