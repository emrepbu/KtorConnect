package com.emrepbu.ktorconnect.server

import android.content.BroadcastReceiver
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emrepbu.ktorconnect.server.ktorserver.ServerManager
import com.emrepbu.ktorconnect.server.ktorserver.ServerStatusReceiver
import com.emrepbu.ktorconnect.server.ui.screens.main.MainScreen
import com.emrepbu.ktorconnect.server.ui.screens.main.MainViewModel
import com.emrepbu.ktorconnect.server.ui.theme.KtorConnectTheme

class MainActivity : ComponentActivity() {

    // Sunucu durum değişikliklerini dinlemek için receiver
    private var serverStatusReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ServerStatusReceiver oluştur
        serverStatusReceiver = ServerStatusReceiver { status ->
            // Sunucu durumu değiştiğinde ServerManager'ı güncelle
            ServerManager.getInstance(this).updateServerStatus(status)
        }

        setContent {
            KtorConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    MainScreen(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Receiver'ı kaydet
        serverStatusReceiver?.let {
            registerReceiver(it, ServerStatusReceiver.getIntentFilter(), RECEIVER_EXPORTED)
        }
    }

    override fun onPause() {
        super.onPause()
        // Receiver'ı kaldır
        serverStatusReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // İstisna durumlarını yok say
            }
        }
    }
}