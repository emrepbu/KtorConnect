package com.emrepbu.ktorconnect.client.ui.screens.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrepbu.ktorconnect.client.data.SampleData
import com.emrepbu.ktorconnect.client.data.StatusMessage
import com.emrepbu.ktorconnect.client.data.StatusType
import com.emrepbu.ktorconnect.client.ktorclient.KtorApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {
    private val apiClient = KtorApiClient()

    // UI states
    var serverIp by mutableStateOf("10.0.2.2")
        private set

    var serverPort by mutableStateOf("8080")
        private set

    var isConnected by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var items by mutableStateOf<List<SampleData>>(emptyList())
        private set

    var selectedItem by mutableStateOf<SampleData?>(null)
        private set

    var statusMessage by mutableStateOf<StatusMessage?>(null)
        private set

    // New item fields
    var newItemName by mutableStateOf("")
        private set

    var newItemValue by mutableStateOf("")
        private set

    // Update server connection details
    fun updateServerIp(ip: String) {
        serverIp = ip
    }

    fun updateServerPort(port: String) {
        serverPort = port
    }

    // Connect to server
    fun connectToServer() {
        if (serverIp.isBlank()) {
            showError("Lütfen sunucu IP adresini girin")
            return
        }

        val port = serverPort.toIntOrNull() ?: 8080

        apiClient.updateServerDetails(serverIp, port)
        isLoading = true

        viewModelScope.launch {
            val result = apiClient.checkConnection()
            isLoading = false

            if (result.isSuccess) {
                isConnected = true
                showSuccess("Sunucuya başarıyla bağlandı")
                println("Sunucuya başarıyla bağlandı")
                startWebSocket()
                refreshData()
            } else {
                isConnected = false
                showError("Bağlantı hatası: ${result.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
                println("Bağlantı hatası: ${result.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
            }
        }
    }

    // Refresh data from server
    fun refreshData() {
        if (!isConnected) return

        isLoading = true

        viewModelScope.launch {
            val result = apiClient.getItems()
            isLoading = false

            if (result.isSuccess) {
                items = result.getOrDefault(emptyList())
                if (items.isEmpty()) {
                    showInfo("Sunucudan veri alındı fakat liste boş")
                } else {
                    showSuccess("${items.size} öğe yüklendi")
                }
            } else {
                showError("Veri yüklenirken hata: ${result.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
            }
        }
    }

    // Select an item for details
    fun selectItem(item: SampleData) {
        selectedItem = item
    }

    // Clear selection
    fun clearSelection() {
        selectedItem = null
    }

    // Update new item form fields
    fun updateNewItemName(name: String) {
        newItemName = name
    }

    fun updateNewItemValue(value: String) {
        newItemValue = value
    }

    // Send new item to server
    fun submitNewItem() {
        if (!isConnected) return

        if (newItemName.isBlank()) {
            showError("Lütfen bir isim girin")
            return
        }

        val value = newItemValue.toDoubleOrNull()
        if (value == null) {
            showError("Lütfen geçerli bir sayı girin")
            return
        }

        val newData = SampleData(
            id = (items.maxOfOrNull { it.id } ?: 0) + 1,
            name = newItemName,
            value = value,
            timestamp = System.currentTimeMillis()
        )

        isLoading = true

        viewModelScope.launch {
            val result = apiClient.submitData(newData)
            isLoading = false

            if (result.isSuccess) {
                showSuccess("Veri başarıyla gönderildi")
                newItemName = ""
                newItemValue = ""
                refreshData()
            } else {
                println(result.exceptionOrNull()?.message)
                showError("Veri gönderilirken hata: ${result.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
            }
        }
    }

    // Status message helpers
    private fun showError(message: String) {
        statusMessage = StatusMessage(message, StatusType.ERROR)
    }

    private fun showSuccess(message: String) {
        statusMessage = StatusMessage(message, StatusType.SUCCESS)
    }

    private fun showInfo(message: String) {
        statusMessage = StatusMessage(message, StatusType.INFO)
    }

    fun clearStatusMessage() {
        statusMessage = null
    }

    // Format timestamp to readable date
    fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    fun startWebSocket() {
        if (!isConnected) return
        println("🧪 WebSocket başlatılıyor...")

        apiClient.connectWebSocket { receivedData ->
            println("🔥 WebSocket ile veri alındı: ${receivedData.name}")
            items = buildList { addAll(items); add(receivedData) }
            showInfo("Yeni veri alındı: ${receivedData.name}")
        }
    }

    // Clean up
    override fun onCleared() {
        apiClient.close()
        super.onCleared()
    }
}
