package io.violabs.webclient.server

import io.violabs.webclient.shared.ApiResponse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicLong

@RestController
@RequestMapping("/api")
class Controller {

    private val requestCounter = AtomicLong(0)

    @GetMapping("/slow-data/{id}")
    suspend fun getSlowData(@PathVariable id: String): ApiResponse {
        val requestId = requestCounter.incrementAndGet()

        // Simulate slow external service (2 seconds)
        delay(Duration.of(2, ChronoUnit.SECONDS))
            .awaitSingleOrNull()

        return ApiResponse(
            id = id,
            data = "Data for $id",
            timestamp = LocalDateTime.now(),
            processingTime = "2000ms",
            requestNumber = requestId,
            threadName = Thread.currentThread().name
        )
    }

    @GetMapping("/fast-data/{id}")
    fun getFastData(@PathVariable id: String): ApiResponse {
        val requestId = requestCounter.incrementAndGet()

        return ApiResponse(
            id = id,
            data = "Quick data for $id",
            timestamp = LocalDateTime.now(),
            processingTime = "0ms",
            requestNumber = requestId,
            threadName = Thread.currentThread().name
        )
    }

    @GetMapping("/health")
    fun health() = mapOf(
        "status" to "UP",
        "timestamp" to LocalDateTime.now(),
        "totalRequests" to requestCounter.get()
    )
}