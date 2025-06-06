package io.violab.webclient.client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class Config {
    
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl("http://localhost:8080")
            .build()
    }
}