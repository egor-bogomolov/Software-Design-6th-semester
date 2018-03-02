package ru.spbau.mit.aush.evaluation.commands

import ru.spbau.mit.aush.evaluation.Environment
import kotlin.concurrent.thread

/**
 * Represents a stand-in for a command which is not built-in
 */
internal class ExternalCommand(private val name: String) : Command() {

    /**
     * {@inheritDoc}
     */
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        val subProcessBuilder = ProcessBuilder(listOf(name) + args)
        subProcessBuilder.environment()?.putAll(environment.variables.data)

        val subProcess = subProcessBuilder.start()
        val subInput = subProcess.inputStream
        val subOutput = subProcess.outputStream
        val subError = subProcess.errorStream

        val pipeStreamThread = thread(true) {
            val input = environment.io.input.bufferedReader()
            val output = subOutput.bufferedWriter()
            val buffer = CharArray(BUFFER_SIZE)

            while (subProcess.isAlive) {
                val readBytes = input.read(buffer)
                if (readBytes == -1) {
                    break
                } else {
                    output.write(buffer, 0, readBytes)
                }
            }
        }

        subProcess.waitFor()
        pipeStreamThread.join()

        subInput.copyTo(environment.io.output)
        subError.copyTo(environment.io.error)
    }

    private companion object {
        const val BUFFER_SIZE = 512
    }
}