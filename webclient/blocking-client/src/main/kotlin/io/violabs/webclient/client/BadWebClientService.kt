package io.violabs.webclient.client

import io.violabs.webclient.shared.ApiResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.time.LocalDateTime

@Service
class BadWebClientService(private val webClient: WebClient) {

    /**
     * BAD EXAMPLE: Using .block() in Spring MVC
     * This will block the servlet thread and cause performance issues
     */
    fun fetchDataBlocking(id: String): ApiResponse {
        println("🚨 [${Thread.currentThread().name}] Starting BLOCKING call for ID: $id at ${LocalDateTime.now()}")

        val startTime = System.currentTimeMillis()

        // THIS IS THE PROBLEM - blocking the servlet thread!
        val response = webClient
            .get()
            .uri("/api/slow-data/{id}", id)
            .retrieve()
            .bodyToMono(ApiResponse::class.java)
            .block(Duration.ofSeconds(10))!! // 🚨 BLOCKING CALL - BAD!

        val endTime = System.currentTimeMillis()

        println("🚨 [${Thread.currentThread().name}] Completed BLOCKING call for ID: $id in ${endTime - startTime}ms")

        return response.copy(
            threadName = Thread.currentThread().name,
            processingTime = "${endTime - startTime}ms"
        )
    }

    /**
     * Multiple blocking calls - even worse!
     */
    fun fetchMultipleDataBlocking(ids: List<String>): List<ApiResponse> {
        println("🚨🚨 [${Thread.currentThread().name}] Starting MULTIPLE BLOCKING calls for ${ids.size} items")

        val startTime = System.currentTimeMillis()

        val responses = ids.map { id ->
            // Each call blocks for 2+ seconds!
            webClient
                .get()
                .uri("/api/slow-data/{id}", id)
                .retrieve()
                .bodyToMono(ApiResponse::class.java)
                .block(Duration.ofSeconds(10))!!
        }

        val endTime = System.currentTimeMillis()
        println("🚨🚨 [${Thread.currentThread().name}] Completed MULTIPLE BLOCKING calls in ${endTime - startTime}ms")

        return responses
    }
}