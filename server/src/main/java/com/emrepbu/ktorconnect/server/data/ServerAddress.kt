package com.emrepbu.ktorconnect.server.data

data class ServerAddress(
    val ip: String,
    val isLocalNetwork: Boolean = true
) {
    companion object {
        // Loopback address for local network
        // 10.0.2.2 is the loopback address for the emulator
        val LOCALHOST = ServerAddress("10.0.2.2", true)
    }
}
