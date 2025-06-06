package io.violab.webclient.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NonblockingClientApplication

fun main(args: Array<String>) {
    runApplication<NonblockingClientApplication>(*args)
}
