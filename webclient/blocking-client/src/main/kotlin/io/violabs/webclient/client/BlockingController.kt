package io.violabs.webclient.client


import io.violabs.webclient.shared.MultipleResponseWrapper
import io.violabs.webclient.shared.ResponseWrapper
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/mvc")
class BlockingController(
    private val badService: BadWebClientService,
    private val betterService: BetterWebClientService
) {

    @GetMapping("/blocking/{id}")
    fun getDataBlocking(@PathVariable id: String): ResponseWrapper {
        val startTime = System.currentTimeMillis()

        println("📥 [${Thread.currentThread().name}] Received request for BLOCKING data: $id")

        val result = badService.fetchDataBlocking(id)

        val endTime = System.currentTimeMillis()

        return ResponseWrapper(
            data = result,
            totalTime = "${endTime - startTime}ms",
            requestThread = Thread.currentThread().name,
            timestamp = LocalDateTime.now(),
            warning = "🚨 This request BLOCKED a servlet thread for ${endTime - startTime}ms!"
        )
    }

    @GetMapping("/blocking-multiple")
    fun getMultipleDataBlocking(@RequestParam ids: List<String>): MultipleResponseWrapper {
        val startTime = System.currentTimeMillis()

        println("📥📥 [${Thread.currentThread().name}] Received request for BLOCKING multiple data: $ids")

        val results = badService.fetchMultipleDataBlocking(ids)

        val endTime = System.currentTimeMillis()

        return MultipleResponseWrapper(
            data = results,
            totalTime = "${endTime - startTime}ms",
            requestThread = Thread.currentThread().name,
            timestamp = LocalDateTime.now(),
            warning = "🚨 This request BLOCKED a servlet thread for ${endTime - startTime}ms! With ${ids.size} sequential calls!"
        )
    }

    @GetMapping("/subscribe-on/{id}")
    fun getDataWithSubscribeOn(@PathVariable id: String): CompletableFuture<ResponseWrapper> {
        val startTime = System.currentTimeMillis()

        println("📥 [${Thread.currentThread().name}] Received request for SCHEDULED data: $id")

        return betterService.fetchDataWithSubscribeOn(id)
            .thenApply { result ->
                val endTime = System.currentTimeMillis()
                ResponseWrapper(
                    data = result,
                    totalTime = "${endTime - startTime}ms",
                    requestThread = Thread.currentThread().name,
                    timestamp = LocalDateTime.now(),
                    warning = "✅ This request used subscribeOn - servlet thread was freed immediately!"
                )
            }
    }

    @GetMapping("/subscribe-on-multiple")
    fun getMultipleDataWithSubscribeOn(@RequestParam ids: List<String>): CompletableFuture<MultipleResponseWrapper> {
        val startTime = System.currentTimeMillis()

        println("📥📥 [${Thread.currentThread().name}] Received request for SCHEDULED multiple data: $ids")

        return betterService.fetchMultipleDataWithSubscribeOn(ids)
            .thenApply { results ->
                val endTime = System.currentTimeMillis()
                MultipleResponseWrapper(
                    data = results,
                    totalTime = "${endTime - startTime}ms",
                    requestThread = Thread.currentThread().name,
                    timestamp = LocalDateTime.now(),
                    warning = "✅ This request used concurrent subscribeOn - servlet thread freed + concurrent execution!"
                )
            }
    }

    @GetMapping("/reactive/{id}")
    fun getDataReactive(@PathVariable id: String): Mono<ResponseWrapper> {
        val startTime = System.currentTimeMillis()

        println("📥 [${Thread.currentThread().name}] Received request for REACTIVE data: $id")

        return betterService.fetchDataAsMono(id)
            .map { result ->
                val endTime = System.currentTimeMillis()
                ResponseWrapper(
                    data = result,
                    totalTime = "${endTime - startTime}ms",
                    requestThread = Thread.currentThread().name,
                    timestamp = LocalDateTime.now(),
                    warning = "🔄 This request returned Mono directly - Spring MVC handles it reactively!"
                )
            }
    }

    @GetMapping("/comparison")
    fun getComparisonInfo(): String {
        return """
            🔥 SPRING MVC + WEBCLIENT APPROACHES:
            
            🚨 BAD - .block():
            - /mvc/blocking/test123
            - /mvc/blocking-multiple?ids=item1,item2,item3
            - Blocks servlet threads (very bad!)
            
            ✅ BETTER - subscribeOn():
            - /mvc/subscribe-on/test123  
            - /mvc/subscribe-on-multiple?ids=item1,item2,item3
            - Uses CompletableFuture + I/O thread pool
            
            🔄 BEST - Return Mono directly:
            - /mvc/reactive/test123
            - Let Spring MVC handle reactively
            
            Key Points:
            - Never use .block() in Spring MVC
            - Use subscribeOn(Schedulers.boundedElastic()) for I/O
            - Return CompletableFuture or Mono from controllers
            - Servlet threads stay free for other requests
            
            Thread dump: http://localhost:8081/actuator/threaddump
            Metrics: http://localhost:8081/actuator/metrics/http.server.requests
        """.trimIndent()
    }
}