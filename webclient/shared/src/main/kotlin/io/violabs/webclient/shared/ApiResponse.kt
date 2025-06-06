package io.violabs.webclient.shared

import java.time.LocalDateTime

data class ApiResponse(
    val id: String,
    val data: String,
    val timestamp: LocalDateTime,
    val processingTime: String,
    val requestNumber: Long,
    val threadName: String
)