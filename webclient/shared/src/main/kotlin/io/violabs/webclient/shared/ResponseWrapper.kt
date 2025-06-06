package io.violabs.webclient.shared

import java.time.LocalDateTime

data class ResponseWrapper(
    val data: ApiResponse,
    val totalTime: String,
    val requestThread: String,
    val timestamp: LocalDateTime,
    val warning: String
)
