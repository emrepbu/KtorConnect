package com.emrepbu.ktorconnect.client.data

data class StatusMessage(
    val message: String,
    val type: StatusType
)

enum class StatusType {
    SUCCESS, ERROR, INFO
}
