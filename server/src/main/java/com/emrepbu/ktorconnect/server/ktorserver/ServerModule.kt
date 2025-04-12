package com.emrepbu.ktorconnect.server.ktorserver

import com.emrepbu.ktorconnect.server.data.SampleData
import com.emrepbu.ktorconnect.server.repository.DataRepository
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun Application.serverModule(
    repository: DataRepository,
    logCallback: (String) -> Unit
) {
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets)

    routing {
        get("/") {
            logCallback("Calling / endpoint")
            call.respondText("Hello, world!")
        }
        get("/api/data") {
            logCallback("Calling /api/data endpoint")
            val data = SampleData(
                id = 1,
                name = "Example Data",
                value = 3.14,
                timestamp = System.currentTimeMillis()
            )
            call.respond(data)
        }

        get("/api/datas") {
            logCallback("Calling /api/datas endpoint")
            val datas = repository.getAllData()
            call.respond(datas)
        }

        get("/api/items/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                logCallback("Invalid ID: null")
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID format"))
                return@get
            }

            val data = repository.getDataById(id)
            if (data == null) {
                logCallback("Not found data: ID=$id")
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Data not found"))
                return@get
            }

            logCallback("Data found: ID=$id")
            call.respond(data)
        }

        post("/api/items") {
            try {
                val receivedData = call.receive<SampleData>()

                val success = repository.addData(receivedData)
                if (success) {
                    logCallback("Add new data: ID=${receivedData.id}")
                    call.respond(HttpStatusCode.Created, mapOf("status" to "success", "message" to "Add data success"))
                } else {
                    logCallback("Could not add data, ID conflict: ID=${receivedData.id}")
                    call.respond(HttpStatusCode.Conflict, mapOf("status" to "error", "message" to "ID conflict"))
                }
            } catch (e: Exception) {
                logCallback("Error while retrieving data: ${e.message}")
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("status" to "error", "message" to "Invalid data format")
                )
            }
        }

        // Öğe güncelle
        put("/api/items/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                logCallback("Invalid ID for update: null")
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID format"))
                return@put
            }

            try {
                val receivedData = call.receive<SampleData>()

                if (receivedData.id != id) {
                    logCallback("ID conflict: Path=$id, Body=${receivedData.id}")
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path ID and Body ID do not match"))
                    return@put
                }

                val success = repository.updateData(receivedData)
                if (success) {
                    logCallback("Data updated: ID=$id")
                    call.respond(mapOf("status" to "success", "message" to "Data updated"))
                } else {
                    logCallback("No data found to update: ID=$id")
                    call.respond(HttpStatusCode.NotFound, mapOf("status" to "error", "message" to "Data not found"))
                }
            } catch (e: Exception) {
                logCallback("Error while retrieving data: ${e.message}")
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("status" to "error", "message" to "Invalid data format")
                )
            }
        }

        delete("/api/items/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                logCallback("Invalid ID for deletion: null")
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID format"))
                return@delete
            }

            val success = repository.deleteDataById(id)
            if (success) {
                logCallback("Data deleted: ID=$id")
                call.respond(mapOf("status" to "success", "message" to "Data deleted"))
            } else {
                logCallback("Silinecek öğe bulunamadı: ID=$id")
                call.respond(HttpStatusCode.NotFound, mapOf("status" to "error", "message" to "Data not found"))
            }
        }

        webSocket("/ws") {
            // Each client connection should be handled in a separate coroutine
            val collectorJob = launch {
                BroadcastChannel.messages.collect { message ->
                    send(Frame.Text(message))
                }
            }

            try {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val text = frame.readText()
                    println("Message from Client: $text")
                }
            } catch (e: Exception) {
                logCallback("Error while retrieving data: ${e.message}")
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("status" to "error", "message" to e.message)
                )
            } finally {
                println("WebSocket connection closed")
                collectorJob.cancel() // collect işini iptal et
            }
        }

        post("/api/broadcast") {
            try {
                val data = call.receive<SampleData>()
                val jsonMessage = Json.encodeToString(data)
                BroadcastChannel.broadcast(jsonMessage)
                logCallback("Veri başarıyla yayınlandı")
                call.respond(mapOf("status" to "success", "message" to "Veri yayınlandı"))
            } catch (e: Exception) {
                logCallback("Yayın hatası: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Yayın başarısız"))
            }
        }
    }
}
