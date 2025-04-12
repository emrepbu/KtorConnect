package com.emrepbu.ktorconnect.server.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.emrepbu.ktorconnect.server.ui.components.LogsPanel
import com.emrepbu.ktorconnect.server.ui.components.ServerAddressCard
import com.emrepbu.ktorconnect.server.ui.components.ServerControlPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val serverStatus by viewModel.serverStatus.collectAsState()
    val serverPort by viewModel.serverPort.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val serverAddresses by viewModel.serverAddresses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android Ktor Sunucu") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ServerControlPanel(
                serverStatus = serverStatus,
                port = serverPort,
                onPortChange = viewModel::updatePort,
                onStartServer = viewModel::startServer,
                onStopServer = viewModel::stopServer,
                onSendBroadcast = viewModel::broadcastData,
            )

            // Sunucu adresi kartını ekleyin
            ServerAddressCard(
                serverStatus = serverStatus,
                serverAddresses = serverAddresses,
                port = serverPort,
                onAddressCopied = viewModel::showAddressCopiedToast
            )

            LogsPanel(
                logs = logs,
                onClearLogs = viewModel::clearLogs
            )
        }
    }
}