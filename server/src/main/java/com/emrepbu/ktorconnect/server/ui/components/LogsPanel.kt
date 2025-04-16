package com.emrepbu.ktorconnect.server.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emrepbu.ktorconnect.server.data.LogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsPanel(
    logs: List<LogEntry>,
    onClearLogs: () -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Server Logs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onClearLogs) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Logs"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "There are no logs yet",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                val listState = rememberLazyListState()

                LaunchedEffect(logs.size) {
                    if (logs.isNotEmpty()) {
                        listState.animateScrollToItem(logs.size - 1)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                ) {
                    items(logs) { log ->
                        LogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(log: LogEntry) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(log.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Text(
            text = "[$formattedTime]",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = log.message,
            color = if (log.isError) Color.Red else MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}
