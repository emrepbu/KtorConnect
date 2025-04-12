package com.emrepbu.ktorconnect.server.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emrepbu.ktorconnect.server.data.ServerStatus

@Composable
fun ServerControlPanel(
    serverStatus: ServerStatus,
    port: String,
    onPortChange: (String) -> Unit,
    onStartServer: () -> Unit,
    onStopServer: () -> Unit,
    onSendBroadcast: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Sunucu Kontrolü",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = port,
                    onValueChange = onPortChange,
                    label = { Text("Port") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                when (serverStatus) {
                    ServerStatus.STOPPED -> {
                        Button(
                            onClick = onStartServer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sunucuyu Başlat")
                        }
                    }

                    ServerStatus.RUNNING -> {
                        Button(
                            onClick = onStopServer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sunucuyu Durdur")
                        }
                    }

                    else -> {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                when (serverStatus) {
                                    ServerStatus.STARTING -> "Başlatılıyor..."
                                    ServerStatus.STOPPING -> "Durduruluyor..."
                                    ServerStatus.ERROR -> "Hata!"
                                    else -> "İşleniyor..."
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ServerStatusIndicator(serverStatus = serverStatus)

            Button(
                onClick = onSendBroadcast,
                enabled = serverStatus == ServerStatus.RUNNING,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Tüm İstemcilere Veri Gönder")
            }
        }
    }
}
