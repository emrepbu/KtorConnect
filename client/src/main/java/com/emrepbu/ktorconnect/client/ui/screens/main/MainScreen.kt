package com.emrepbu.ktorconnect.client.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emrepbu.ktorconnect.client.ui.components.ConnectionSection
import com.emrepbu.ktorconnect.client.ui.components.ItemCard
import com.emrepbu.ktorconnect.client.ui.components.ItemDetailDialog
import com.emrepbu.ktorconnect.client.ui.components.NewItemForm
import com.emrepbu.ktorconnect.client.ui.components.StatusMessageBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var showNewItemForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ktor İstemci") },
                actions = {
                    if (viewModel.isConnected) {
                        IconButton(onClick = { viewModel.refreshData() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (viewModel.isConnected && !showNewItemForm) {
                FloatingActionButton(
                    onClick = { showNewItemForm = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Yeni Veri Ekle")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Status message bar at the top
                viewModel.statusMessage?.let { message ->
                    StatusMessageBar(
                        statusMessage = message,
                        onDismiss = { viewModel.clearStatusMessage() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Connection Section
                ConnectionSection(
                    serverIp = viewModel.serverIp,
                    serverPort = viewModel.serverPort,
                    isConnected = viewModel.isConnected,
                    onIpChange = viewModel::updateServerIp,
                    onPortChange = viewModel::updateServerPort,
                    onConnectClick = viewModel::connectToServer,
                    isLoading = viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // New Item Form (conditional)
                if (showNewItemForm) {
                    NewItemForm(
                        name = viewModel.newItemName,
                        value = viewModel.newItemValue,
                        onNameChange = viewModel::updateNewItemName,
                        onValueChange = viewModel::updateNewItemValue,
                        onSubmit = {
                            viewModel.submitNewItem()
                            showNewItemForm = false
                        },
                        isLoading = viewModel.isLoading
                    )
                }

                // Data list section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (viewModel.isConnected) {
                        Text(
                            text = "Veriler",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (viewModel.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (viewModel.items.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Henüz veri bulunmuyor",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            LazyColumn {
                                items(viewModel.items.size) { id ->
                                    ItemCard(
                                        item = viewModel.items[id],
                                        formatTimestamp = viewModel::formatTimestamp,
                                        onClick = viewModel::selectItem
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sunucuya bağlanın",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Item Detail Dialog
            viewModel.selectedItem?.let { item ->
                ItemDetailDialog(
                    item = item,
                    formatTimestamp = viewModel::formatTimestamp,
                    onDismiss = viewModel::clearSelection
                )
            }
        }
    }
}
