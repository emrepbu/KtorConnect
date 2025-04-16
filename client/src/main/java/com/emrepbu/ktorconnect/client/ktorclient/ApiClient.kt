package com.emrepbu.ktorconnect.client.ktorclient

import com.emrepbu.ktorconnect.client.data.SampleData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class KtorApiClient {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(io.ktor.client.plugins.websocket.WebSockets)

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }

        engine {
            pipelining = false
        }
    }

    // Server connection details
    private var serverIp: String = ""
    private var serverPort: Int = 8080

    fun updateServerDetails(ip: String, port: Int) {
        serverIp = ip
        serverPort = port
    }

    // Base URL builder
    private fun buildUrl(endpoint: String): String {
        val baseUrl = if (serverIp.startsWith("http://")) serverIp else "http://$serverIp"
        return "$baseUrl:$serverPort$endpoint"
    }

    // Check server connection
    suspend fun checkConnection(): Result<String> {
        return try {
            val response: HttpResponse = client.get(buildUrl("/"))
            if (response.status.isSuccess()) {
                Result.success(response.bodyAsText())
            } else {
                Result.failure(Exception("Server responded with: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get single data item
    suspend fun getData(): Result<SampleData> {
        return try {
            val response: SampleData = client.get(buildUrl("/api/data")).body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get list of items
    suspend fun getItems(): Result<List<SampleData>> {
        return try {
            val response: List<SampleData> = client.get(buildUrl("/api/items")).body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Send data to server
    suspend fun submitData(data: SampleData): Result<Map<String, String>> {
        return try {
            val response: Map<String, String> = client.post(buildUrl("/api/submit")) {
                contentType(ContentType.Application.Json)
                setBody(data)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Close client when done
    fun close() {
        client.close()
    }

    private val _isWebSocketConnected = MutableStateFlow(false)
    val isWebSocketConnected: StateFlow<Boolean> = _isWebSocketConnected.asStateFlow()

    private val _incomingMessages = MutableStateFlow<List<SampleData>>(emptyList())
    val incomingMessages: StateFlow<List<SampleData>> = _incomingMessages.asStateFlow()

    private var webSocketSession: DefaultClientWebSocketSession? = null
    private var webSocketJob: Job? = null

    fun connectWebSocket(callback: (SampleData) -> Unit) {
        if (webSocketJob?.isActive == true) {
            return
        }

        webSocketJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = serverIp.removePrefix("http://").removePrefix("https://"),
                    port = serverPort,
                    path = "/ws"
                ) {
                    webSocketSession = this
                    _isWebSocketConnected.value = true

                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val text = frame.readText()
                        println("Raw data: $text")

                        try {
                            val data = Json.decodeFromString<SampleData>(text)

                            withContext(Dispatchers.Main) {
                                println("Data: ${data.name}")
                                callback(data)
                            }

                            _incomingMessages.value = _incomingMessages.value + data
                        } catch (e: Exception) {
                            println("${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("${e.message}")
            } finally {
                _isWebSocketConnected.value = false
                webSocketSession = null
            }
        }
    }

    fun disconnectWebSocket() {
        webSocketJob?.cancel()
        webSocketJob = null
        webSocketSession = null
        _isWebSocketConnected.value = false
    }
}
