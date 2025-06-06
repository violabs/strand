package io.violabs.threads.threads

class BasicRunnable(private val actualMethod: (() -> Unit)? = null) : Runnable {
    override fun run() {
        println("Running BasicRunnable")
        actualMethod?.invoke()
    }
}