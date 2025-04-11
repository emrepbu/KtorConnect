package com.emrepbu.ktorconnect.server.data

import kotlinx.serialization.Serializable

/**
 * A data class representing a sample data item.
 */
@Serializable
data class SampleData(
    val id: Int,
    val name: String,
    val value: Double,
    val timestamp: Long = System.currentTimeMillis()
)