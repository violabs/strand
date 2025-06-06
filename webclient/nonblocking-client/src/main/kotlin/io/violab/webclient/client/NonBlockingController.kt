package io.violab.webclient.client
import io.violabs.webclient.shared.MultipleResponseWrapper
import io.violabs.webclient.shared.ResponseWrapper
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/non-blocking")
class NonBlockingController(private val goodService: GoodWebClientService) {
    
    @GetMapping("/single/{id}")
    suspend fun getSingleData(@PathVariable id: String): ResponseWrapper {
        val startTime = System.currentTimeMillis()
        
        println("📥 [${Thread.currentThread().name}] Received request for single data: $id")
        
        val result = goodService.fetchDataNonBlocking(id)
        
        val endTime = System.currentTimeMillis()
        
        return ResponseWrapper(
            data = result,
            totalTime = "${endTime - startTime}ms",
            requestThread = Thread.currentThread().name,
            timestamp = LocalDateTime.now(),
            warning = "This request used coroutines - no threads were blocked!"
        )
    }
    
    @GetMapping("/multiple")
    suspend fun getMultipleData(@RequestParam ids: List<String>): MultipleResponseWrapper {
        val startTime = System.currentTimeMillis()
        
        println("📥📥 [${Thread.currentThread().name}] Received request for multiple data: $ids")
        
        val results = goodService.fetchMultipleDataNonBlocking(ids)
        
        val endTime = System.currentTimeMillis()
        
        return MultipleResponseWrapper(
            data = results,
            totalTime = "${endTime - startTime}ms",
            requestThread = Thread.currentThread().name,
            timestamp = LocalDateTime.now(),
            warning = "This request used concurrent coroutines - much faster and no blocked threads!"
        )
    }
    
    @GetMapping("/comparison")
    fun getComparisonInfo(): String {
        return """
            🔥 NON-BLOCKING CLIENT BENEFITS:
            
            ✅ Threads are never blocked - they can handle other requests
            ✅ Better resource utilization
            ✅ Higher throughput under load
            ✅ Multiple calls happen concurrently (not sequentially)
            ✅ More scalable
            
            Try these endpoints:
            - Single: http://localhost:8082/non-blocking/single/test123
            - Multiple: http://localhost:8082/non-blocking/multiple?ids=item1,item2,item3
            
            Compare with blocking client performance!
            
            Thread dump: http://localhost:8082/actuator/threaddump
            Metrics: http://localhost:8082/actuator/metrics/http.server.requests
        """.trimIndent()
    }
}