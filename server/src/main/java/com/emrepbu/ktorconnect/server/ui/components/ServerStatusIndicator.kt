package com.emrepbu.ktorconnect.server.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emrepbu.ktorconnect.server.data.ServerStatus

@Composable
fun ServerStatusIndicator(serverStatus: ServerStatus) {
    val (statusColor, statusText) = when (serverStatus) {
        ServerStatus.RUNNING -> Pair(Color.Green, "Çalışıyor")
        ServerStatus.STOPPED -> Pair(Color.Gray, "Durduruldu")
        ServerStatus.STARTING -> Pair(Color.Yellow, "Başlatılıyor")
        ServerStatus.STOPPING -> Pair(Color.Yellow, "Durduruluyor")
        ServerStatus.ERROR -> Pair(Color.Red, "Hata!")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Durum:",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(statusColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = statusText,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
