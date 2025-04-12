package com.emrepbu.ktorconnect.client.data

import kotlinx.serialization.Serializable

@Serializable
data class SampleData(
    val id: Int,
    val name: String,
    val value: Double,
    val timestamp: Long
)
