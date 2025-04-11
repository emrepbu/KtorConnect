package com.emrepbu.ktorconnect.server.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.emrepbu.ktorconnect.server.MainActivity
import com.emrepbu.ktorconnect.server.R
import com.emrepbu.ktorconnect.server.manager.ServerManager
import com.emrepbu.ktorconnect.server.repository.DataRepository
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.Job
import java.util.concurrent.TimeUnit

class ServerService : Service() {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private var serverJob: Job? = null

    private lateinit var serverManager: ServerManager
    private lateinit var dataRepository: DataRepository

    override fun onCreate() {
        super.onCreate()
        serverManager = ServerManager(this)
        dataRepository = DataRepository.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_START -> {
                val port = intent.getIntExtra("port", 8080)
                startServer(port)
            }

            ACTION_STOP -> {
                stopServer()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startServer(port: Int) {
        if (server != null) {
            serverManager.addLog("Server is already running.")
        }

        try {
            // TODO: start service
//            startForeground(NOTIFICATION_ID, createNotification())

        } catch (e: Exception) {

        }

    }

    private fun stopServer() {
        serverJob?.cancel()
        val server = this.server
        if (server == null) {
            serverManager.addLog("Server is not running.")
        } else {
            server.stop(1, 2, TimeUnit.SECONDS)
            serverManager.addLog("Server stopped.")
        }
    }

    private fun createNotification(): Notification {
        val channelId = "server_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification channel for API 26+
        val channel = NotificationChannel(
            channelId,
            "Server Channel",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Ktor Connect notification channel"
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(channel)

        // Create pending intent for notification click
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Android Sunucu")
            .setContentText("Sunucu çalışıyor")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.emrepbu.ktorconnect.server.action.START_SERVER"
        const val ACTION_STOP = "com.emrepbu.ktorconnect.server.action.STOP_SERVER"

        private const val DEFAULT_PORT = 8080
        private const val NOTIFICATION_ID = 1
    }
}
