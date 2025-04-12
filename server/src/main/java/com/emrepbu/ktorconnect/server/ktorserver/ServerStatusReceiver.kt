package com.emrepbu.ktorconnect.server.ktorserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.emrepbu.ktorconnect.server.data.ServerStatus

/**
 * Sunucu durumu için BroadcastReceiver.
 * Bu sınıf, servis ve aktivite/fragment arasında iletişimi kolaylaştırır.
 */
class ServerStatusReceiver(private val onStatusChange: (ServerStatus) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SERVER_STATUS_CHANGED -> {
                val statusOrdinal = intent.getIntExtra(EXTRA_SERVER_STATUS, ServerStatus.STOPPED.ordinal)
                val status = ServerStatus.values()[statusOrdinal]
                onStatusChange(status)
            }
        }
    }

    companion object {
        const val ACTION_SERVER_STATUS_CHANGED = "com.example.androidserver.ACTION_SERVER_STATUS_CHANGED"
        const val EXTRA_SERVER_STATUS = "server_status"

        /**
         * IntentFilter oluşturur
         */
        fun getIntentFilter(): IntentFilter {
            return IntentFilter(ACTION_SERVER_STATUS_CHANGED)
        }

        /**
         * Sunucu durumunu broadcast eden Intent oluşturur
         */
        fun createStatusIntent(status: ServerStatus): Intent {
            return Intent(ACTION_SERVER_STATUS_CHANGED).apply {
                putExtra(EXTRA_SERVER_STATUS, status.ordinal)
            }
        }

        /**
         * Sunucu durumunu broadcast eder
         */
        fun broadcastStatus(context: Context, status: ServerStatus) {
            val intent = createStatusIntent(status)
            context.sendBroadcast(intent)
        }
    }
}
