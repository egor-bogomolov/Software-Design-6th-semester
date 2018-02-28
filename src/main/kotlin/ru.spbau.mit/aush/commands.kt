package ru.spbau.mit.aush

import java.io.File
import java.io.InputStream
import java.io.PrintStream
import kotlin.concurrent.thread

/**
 * Represents shell command
 */
sealed class Command {
    /**
     * Runs the command with given arguments and in the given environment
     *
     * @param args list of arguments
     * @param environment environment in which the command should be running
     */
    abstract fun evaluate(
            args: List<String> = emptyList(),
            environment: Environment
    )

    companion object {
        /**
         * Gets the right command by its name
         * If there is no such built-in command then external shell command will be executed
         *
         * @param name name of the command
         * @return corresponding command
         */
        fun getCommand(name: String): Command = commands[name] ?: ExternalCommand(name)

        private val commands = mapOf(
                "cat" to CatCommand,
                "echo" to EchoCommand,
                "wc" to WcCommand,
                "pwd" to PwdCommand,
                "exit" to ExitCommand
        )
    }
}

/**
 * Represents simplified version of `cat` Bash command
 * Outputs contents of files given in its arguments
 * If no arguments are provided then input stream is outputted
 */
private object CatCommand : Command() {
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        if (args.isEmpty()) {
            environment.io.input.copyTo(environment.io.output)
        } else {
            val printStream = PrintStream(environment.io.output)

            for (fileName in args) {
                File(fileName).inputStream().copyTo(environment.io.output)
                printStream.println()
            }
        }
    }
}

/**
 * Represents simplified version of `echo` Bash command
 * Outputs its arguments separated by whitespace character on a new line
 */
private object EchoCommand : Command() {
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        PrintStream(environment.io.output).println(
                args.joinToString(" ")
        )
    }
}

/**
 * Represents simplified version of `wc` Bash command
 * For each of its arguments outputs number of lines, words and bytes in a corresponding file
 * Also outputs total counts
 * If no arguments are given then outputs same statistics for its input stream
 */
private object WcCommand : Command() {
    private data class Stats(
            val lines: Int,
            val words: Int,
            val bytes: Int) {
        operator fun plus(other: Stats) =
                Stats(
                        lines + other.lines,
                        words + other.words,
                        bytes + other.bytes
                )

        fun toPrintStream(output: PrintStream, comment: String = "") {
            output.print("\t$lines")
            output.print("\t$words")
            output.print("\t$bytes")
            output.println(if (comment.isEmpty()) "" else "\t$comment")
        }
    }

    private fun InputStream.calculateStats(): Stats {
        val lines = bufferedReader().readLines()
        return Stats(
                lines.size,
                lines.sumBy { it.split(' ', '\t', '\n').size },
                lines.sumBy { it.length }
        )
    }

    private fun File.calculateStats(): Stats = inputStream().calculateStats()

    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        val printStream = PrintStream(environment.io.output)

        when (args.size) {
            0 -> environment.io.input
                    .calculateStats()
                    .toPrintStream(printStream)
            1 -> File(args[0])
                    .calculateStats()
                    .toPrintStream(printStream, args[0])
            2 -> {
                var total = Stats(0, 0, 0)

                for (filename in args) {
                    val fileStats = File(filename).calculateStats()
                    fileStats.toPrintStream(printStream, filename)
                    total += fileStats
                }

                total.toPrintStream(printStream, "total")
            }
        }
    }
}

/**
 * Represents simplified version of `pwd` Bash command
 * Outputs current working directory
 */
private object PwdCommand : Command() {
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        PrintStream(environment.io.output).println(System.getProperty("user.dir"))
    }
}

/**
 * Represents simplified version of `exit` Bash command
 * Exits the shell
 */
object ExitCommand : Command() {
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {

    }
}

/**
 * Represents a stand-in for a command which is not built-in
 */
private data class ExternalCommand(val name: String) : Command() {
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