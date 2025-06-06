package io.violab.webclient.client

import io.violabs.webclient.shared.ApiResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDateTime

@Service
class GoodWebClientService(private val webClient: WebClient) {
    
    /**
     * GOOD EXAMPLE: Using suspend functions with WebClient
     * This doesn't block threads - it suspends the coroutine
     */
    suspend fun fetchDataNonBlocking(id: String): ApiResponse {
        println("✅ [${Thread.currentThread().name}] Starting NON-BLOCKING call for ID: $id at ${LocalDateTime.now()}")
        
        val startTime = System.currentTimeMillis()
        
        // This suspends the coroutine but doesn't block the thread!
        val response = webClient
            .get()
            .uri("/api/slow-data/{id}", id)
            .retrieve()
            .awaitBody<ApiResponse>() // ✅ NON-BLOCKING - GOOD!
        
        val endTime = System.currentTimeMillis()
        
        println("✅ [${Thread.currentThread().name}] Completed NON-BLOCKING call for ID: $id in ${endTime - startTime}ms")
        
        return response.copy(
            threadName = Thread.currentThread().name,
            processingTime = "${endTime - startTime}ms"
        )
    }
    
    /**
     * Multiple concurrent calls - much better!
     */
    suspend fun fetchMultipleDataNonBlocking(ids: List<String>): List<ApiResponse> = coroutineScope {
        println("✅✅ [${Thread.currentThread().name}] Starting CONCURRENT NON-BLOCKING calls for ${ids.size} items")
        
        val startTime = System.currentTimeMillis()
        
        // All calls happen concurrently!
        val responses = ids.map { id ->
            async {
                webClient
                    .get()
                    .uri("/api/slow-data/{id}", id)
                    .retrieve()
                    .awaitBody<ApiResponse>()
            }
        }.awaitAll()
        
        val endTime = System.currentTimeMillis()
        println("✅✅ [${Thread.currentThread().name}] Completed CONCURRENT NON-BLOCKING calls in ${endTime - startTime}ms")
        
        responses
    }
    
    /**
     * Alternative using Mono - also good
     */
    suspend fun fetchDataWithMono(id: String): ApiResponse {
        return webClient
            .get()
            .uri("/api/slow-data/{id}", id)
            .retrieve()
            .bodyToMono(ApiResponse::class.java)
            .awaitSingle() // Convert Mono to suspend function
    }
}