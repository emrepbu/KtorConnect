package com.emrepbu.ktorconnect.server.ui.screens.main

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emrepbu.ktorconnect.server.data.LogEntry
import com.emrepbu.ktorconnect.server.data.SampleData
import com.emrepbu.ktorconnect.server.data.ServerAddress
import com.emrepbu.ktorconnect.server.data.ServerStatus
import com.emrepbu.ktorconnect.server.ktorserver.ServerManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    // Server manager instance
    private val serverManager = ServerManager.getInstance(application)
    private var broadcastCounter = 0

    // Server addresses
    val serverAddresses: StateFlow<List<ServerAddress>> = serverManager.serverAddresses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Adres kopyalandığında kullanıcıya bildirim göstermek için fonksiyon ekleyin:
    fun showAddressCopiedToast() {
        Toast.makeText(
            getApplication<Application>().applicationContext,
            "Sunucu adresi panoya kopyalandı",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Broadcast fonksiyonu
    fun broadcastData() {
        viewModelScope.launch {
            try {
                broadcastCounter++

                val dataToSend = SampleData(
                    id = broadcastCounter,
                    name = "Sunucudan Gönderilen #$broadcastCounter",
                    value = Math.random() * 100, // Rastgele değer
                    timestamp = System.currentTimeMillis()
                )

                // HTTP isteği yap
                val httpClient = HttpClient(Android) {
                    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                        json()
                    }
                }
                val ip = serverAddresses.value.firstOrNull { it.ip != "localhost" }?.ip ?: "localhost"
                val url = "http://$ip:${serverPort.value}/api/broadcast"

                httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(dataToSend)
                }

                serverManager.addLog("Veri yayını başarıyla gönderildi")
            } catch (e: Exception) {
                println("Veri yayını başarısız: ${e.message}")
                serverManager.addLog("Veri yayını başarısız: ${e.message}", true)
            }
        }
    }

    // Server status
    val serverStatus: StateFlow<ServerStatus> = serverManager.serverStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ServerStatus.STOPPED
        )

    // Server port
    private val _serverPort = MutableStateFlow("8080")
    val serverPort: StateFlow<String> = _serverPort.asStateFlow()

    // Server logs
    val logs: StateFlow<List<LogEntry>> = serverManager.logs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Port değişikliği
    fun updatePort(port: String) {
        _serverPort.value = port
    }

    // Sunucuyu başlat
    fun startServer() {
        val port = _serverPort.value.toIntOrNull() ?: 8080
        viewModelScope.launch(Dispatchers.IO) {
            serverManager.startServer(port)
        }
    }

    // Sunucuyu durdur
    fun stopServer() {
        viewModelScope.launch(Dispatchers.IO) {
            serverManager.stopServer()
        }
    }

    // Logları temizle
    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            serverManager.clearLogs()
        }
    }
}
