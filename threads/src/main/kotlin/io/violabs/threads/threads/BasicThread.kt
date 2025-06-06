package io.violabs.threads.threads

class BasicThread(private val runnable: Runnable? = null) : Thread() {
    override fun run() {
        println("Running BasicThread")
        runnable?.run()
    }
}