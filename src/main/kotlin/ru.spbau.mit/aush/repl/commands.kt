package ru.spbau.mit.aush.repl

import java.io.File
import java.io.InputStream
import java.io.OutputStream

sealed class Command {
    abstract fun evaluate(
            args: List<String> = emptyList(),
            environment: Environment
    )

    companion object {
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

private object CatCommand : Command() {
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        if (args.isEmpty()) {
            environment.input.copyTo(environment.output)
        } else {
            for (fileName in args) {
                File(fileName).inputStream().copyTo(environment.output)
            }
        }
    }
}

private object EchoCommand : Command() {
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        environment.output.writer().use {
            for (string in args) {
                it.append(string)
            }
            it.appendln()
        }
    }
}

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

        fun toOutputStream(output: OutputStream, comment: String = "") =
                output
                        .writer()
                        .append("\t$lines")
                        .append("\t$words")
                        .append("\t$bytes")
                        .appendln(if (comment.isEmpty()) "" else "\t$comment")
    }

    private fun InputStream.calculateStats(): Stats =
            reader().readText().let {text ->
                Stats(
                        text.lines().size,
                        text.split(' ', '\n', '\t').size,
                        text.length
                )
            }

    private fun File.calculateStats(): Stats = inputStream().calculateStats()

    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        when (args.size) {
            0 -> environment.input
                    .calculateStats()
                    .toOutputStream(environment.output)
            1 -> File(args[0])
                    .calculateStats()
                    .toOutputStream(environment.output, args[0])
            2 -> {
                var total = Stats(0, 0, 0)

                for (filename in args) {
                    val fileStats = File(filename).calculateStats()
                    fileStats.toOutputStream(environment.output, filename)
                    total += fileStats
                }

                total.toOutputStream(environment.output, "total")
            }
        }
    }
}

private object PwdCommand : Command() {
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        environment.output.writer().appendln(System.getProperty("user.dir"))
    }
}

private object ExitCommand : Command() {
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        System.exit(0)
    }
}

private class ExternalCommand(val name: String) : Command() {
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        val subProcessBuilder = ProcessBuilder(listOf(name) + args)
        subProcessBuilder.environment()?.putAll(environment.variables)

        val subProcess = subProcessBuilder.start()
        val subInput = subProcess.inputStream
        val subOutput = subProcess.outputStream
        val subError = subProcess.errorStream

        environment.input.copyTo(subOutput)
        subProcess.waitFor()
        subInput.copyTo(environment.output)
        subError.copyTo(environment.error)
    }
}