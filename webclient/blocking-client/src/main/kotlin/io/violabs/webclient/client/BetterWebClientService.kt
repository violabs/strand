package io.violabs.webclient.client
import io.violabs.webclient.shared.ApiResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@Service 
class BetterWebClientService(private val webClient: WebClient) {
    
    /**
     * BETTER EXAMPLE: Using subscribeOn with proper scheduler
     * This offloads the blocking I/O to a separate thread pool
     */
    fun fetchDataWithSubscribeOn(id: String): CompletableFuture<ApiResponse> {
        println("✅ [${Thread.currentThread().name}] Starting SCHEDULED call for ID: $id at ${LocalDateTime.now()}")
        
        val startTime = System.currentTimeMillis()
        
        return webClient
            .get()
            .uri("/api/slow-data/{id}", id)
            .retrieve()
            .bodyToMono(ApiResponse::class.java)
            .subscribeOn(Schedulers.boundedElastic()) // 🔄 Move to I/O thread pool
            .doOnNext { response ->
                val endTime = System.currentTimeMillis()
                println("✅ [${Thread.currentThread().name}] Completed SCHEDULED call for ID: $id in ${endTime - startTime}ms")
            }
            .map { response ->
                response.copy(
                    threadName = Thread.currentThread().name,
                    processingTime = "${System.currentTimeMillis() - startTime}ms"
                )
            }
            .toFuture()
    }
    
    /**
     * Multiple concurrent calls using subscribeOn - much better!
     */
    fun fetchMultipleDataWithSubscribeOn(ids: List<String>): CompletableFuture<List<ApiResponse>> {
        println("✅✅ [${Thread.currentThread().name}] Starting CONCURRENT SCHEDULED calls for ${ids.size} items")
        
        val startTime = System.currentTimeMillis()
        
        return Flux.fromIterable(ids)
            .flatMap { id ->
                webClient
                    .get()
                    .uri("/api/slow-data/{id}", id)
                    .retrieve()
                    .bodyToMono(ApiResponse::class.java)
                    .subscribeOn(Schedulers.boundedElastic()) // Each call on separate I/O thread
            }
            .collectList()
            .doOnNext {
                val endTime = System.currentTimeMillis()
                println("✅✅ [${Thread.currentThread().name}] Completed CONCURRENT SCHEDULED calls in ${endTime - startTime}ms")
            }
            .toFuture()
    }
    
    /**
     * Return Mono directly - let Spring handle it
     */
    fun fetchDataAsMono(id: String): Mono<ApiResponse> {
        println("🔄 [${Thread.currentThread().name}] Starting REACTIVE call for ID: $id")
        
        return webClient
            .get()
            .uri("/api/slow-data/{id}", id)
            .retrieve()
            .bodyToMono(ApiResponse::class.java)
            .subscribeOn(Schedulers.boundedElastic())
            .map { response ->
                response.copy(
                    threadName = Thread.currentThread().name,
                    processingTime = "Reactive - no blocking"
                )
            }
    }
}