package com.emrepbu.ktorconnect.client.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emrepbu.ktorconnect.client.data.StatusMessage
import com.emrepbu.ktorconnect.client.data.StatusType
import kotlinx.coroutines.delay

@Composable
fun StatusMessageBar(
    statusMessage: StatusMessage?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = statusMessage != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        statusMessage?.let { message ->
            val backgroundColor = when (message.type) {
                StatusType.SUCCESS -> Color(0xFF388E3C)
                StatusType.ERROR -> Color(0xFFD32F2F)
                StatusType.INFO -> Color(0xFF1976D2)
            }

            val icon = when (message.type) {
                StatusType.SUCCESS -> Icons.Default.CheckCircle
                StatusType.ERROR -> Icons.Default.Error
                StatusType.INFO -> Icons.Default.Info
            }

            // Auto-dismiss after 3 seconds
            LaunchedEffect(message) {
                delay(3000)
                onDismiss()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Status Icon",
                        tint = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = message.message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
