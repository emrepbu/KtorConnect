package com.emrepbu.ktorconnect.server.ktorserver

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.emrepbu.ktorconnect.server.MainActivity
import com.emrepbu.ktorconnect.server.R
import com.emrepbu.ktorconnect.server.data.ServerStatus
import com.emrepbu.ktorconnect.server.repository.DataRepository
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Arka planda çalışan sunucu servisi.
 */
class ServerService : Service() {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private var serverJob: Job? = null
    private lateinit var serverManager: ServerManager
    private lateinit var dataRepository: DataRepository

    override fun onCreate() {
        super.onCreate()
        serverManager = ServerManager.getInstance(this)
        dataRepository = DataRepository.Companion.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_START -> {
                val port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT)
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

    @SuppressLint("ForegroundServiceType")
    private fun startServer(port: Int) {
        if (server != null) {
            serverManager.addLog("Sunucu zaten çalışıyor")
            return
        }

        // Daha detaylı loglar ekle
        serverManager.addLog("Sunucu başlatılıyor... PORT: $port")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(NOTIFICATION_ID, createNotification())
            }
            // Başlatma işlemini ayrı bir coroutine'de yap
            serverJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    // İlk adım: Sunucuyu oluştur
                    serverManager.addLog("Netty sunucusu oluşturuluyor...")
                    val serverInstance = embeddedServer(Netty, port = port) {
                        serverModule(
                            repository = dataRepository,
                            logCallback = { message ->
                                serverManager.addLog(message)
                            }
                        )
                    }

                    // İkinci adım: Sunucuyu başlat
                    serverManager.addLog("Netty sunucusu başlatılıyor...")
                    serverInstance.start(wait = false)

                    // Sunucu referansını sakla
                    server = serverInstance

                    // Başarılı ise durumu güncelle
                    serverManager.addLog("Sunucu başarıyla çalışıyor!")
                    serverManager.updateServerStatus(ServerStatus.RUNNING)
                } catch (e: Exception) {
                    val errorMessage = "Sunucu başlatılamadı: ${e.message ?: "Bilinmeyen hata"}"
                    serverManager.addLog(errorMessage, true)
                    serverManager.addLog("Detay: ${e.stackTraceToString()}", true)
                    println(e.stackTraceToString())
                    serverManager.updateServerStatus(ServerStatus.ERROR)
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            val errorMessage = "Servis başlatılamadı: ${e.message ?: "Bilinmeyen hata"}"
            println(errorMessage)
            serverManager.addLog(errorMessage, true)
            serverManager.updateServerStatus(ServerStatus.ERROR)
            stopSelf()
        }
    }

    private fun stopServer() {
        serverJob?.cancel()

        // Durdurulma durumunu broadcast et
        ServerStatusReceiver.broadcastStatus(this, ServerStatus.STOPPING)

        val server = this.server
        if (server != null) {
            try {
                server.stop(1, 2, TimeUnit.SECONDS)
            } catch (e: Exception) {
                serverManager.addLog("Sunucu durdurulurken hata: ${e.message}", true)
            } finally {
                this.server = null
                serverManager.updateServerStatus(ServerStatus.STOPPED)
                // Durduruldu durumunu broadcast et
                ServerStatusReceiver.broadcastStatus(this, ServerStatus.STOPPED)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        } else {
            serverManager.updateServerStatus(ServerStatus.STOPPED)
            // Durduruldu durumunu broadcast et
            ServerStatusReceiver.broadcastStatus(this, ServerStatus.STOPPED)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotification(): Notification {
        val channelId = "server_service_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Notification channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Server Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Android Server notifications"
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create pending intent for notification tap
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
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
        const val EXTRA_PORT = "com.emrepbu.ktorconnect.server.extra.PORT"

        private const val DEFAULT_PORT = 8080
        private const val NOTIFICATION_ID = 1
    }
}