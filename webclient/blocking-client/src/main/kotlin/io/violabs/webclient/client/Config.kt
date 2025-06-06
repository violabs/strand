package io.violabs.webclient.client

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class Config {

    @Bean
    fun webClient(): WebClient {
        // Use HttpComponents instead of Netty to ensure servlet stack
        return WebClient.builder()
            .baseUrl("http://localhost:8080")
            .build()
    }
}