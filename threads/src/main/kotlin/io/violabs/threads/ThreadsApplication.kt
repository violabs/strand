package io.violabs.threads

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ThreadsApplication

fun main(args: Array<String>) {
    runApplication<ThreadsApplication>(*args)
}
