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
            showError("Please enter a server IP")
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
                showSuccess("Successfully connected to the server")
                startWebSocket()
                refreshData()
            } else {
                isConnected = false
                showError("Connection error: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
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
                    showInfo("Data received from server but list is empty")
                } else {
                    showSuccess("${items.size} item updated")
                }
            } else {
                showError("Error loading data: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
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
            showError("Please enter a name")
            return
        }

        val value = newItemValue.toDoubleOrNull()
        if (value == null) {
            showError("Please enter a valid value")
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
                showSuccess("Data sent successfully")
                newItemName = ""
                newItemValue = ""
                refreshData()
            } else {
                println(result.exceptionOrNull()?.message)
                showError("Error loading data: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
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
        apiClient.connectWebSocket { receivedData ->
            items = buildList { addAll(items); add(receivedData) }
            showInfo("${receivedData.name} data received")
        }
    }

    // Clean up
    override fun onCleared() {
        apiClient.close()
        super.onCleared()
    }
}
