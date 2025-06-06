package io.violabs.webclient.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BlockingClientApplication

fun main(args: Array<String>) {
    runApplication<BlockingClientApplication>(*args)
}
