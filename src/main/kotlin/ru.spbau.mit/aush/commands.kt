package ru.spbau.mit.aush

import java.io.File
import java.io.InputStream
import java.io.OutputStream

sealed class Command {
    abstract fun evaluate(
            args : List<String> = emptyList(),
            input : InputStream = System.`in`,
            output : OutputStream = System.out,
            error : OutputStream = System.err,
            environment: Map<String,String>
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
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: Map<String,String>
    ) {
        if (args.isEmpty()) {
            input.copyTo(output)
        } else {
            for (fileName in args) {
                File(fileName).inputStream().copyTo(output)
            }
        }
    }
}

private object EchoCommand : Command() {
    override fun evaluate(
            args: List<String>,
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: Map<String,String>
    ) {
        output.writer().use {
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
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: Map<String,String>
    ) {
        when (args.size) {
            0 -> input
                    .calculateStats()
                    .toOutputStream(output)
            1 -> File(args[0])
                    .calculateStats()
                    .toOutputStream(output, args[0])
            2 -> {
                var total = Stats(0, 0, 0)

                for (filename in args) {
                    val fileStats = File(filename).calculateStats()
                    fileStats.toOutputStream(output, filename)
                    total += fileStats
                }

                total.toOutputStream(output, "total")
            }
        }
    }
}

private object PwdCommand : Command() {
    override fun evaluate(
            args: List<String>,
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: Map<String,String>
    ) {
        output.writer().appendln(System.getProperty("user.dir"))
    }
}

private object ExitCommand : Command() {
    override fun evaluate(
            args: List<String>,
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: Map<String,String>
    ) {
        System.exit(0)
    }
}

private class ExternalCommand(val name: String) : Command() {
    override fun evaluate(
            args: List<String>,
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: Map<String,String>
    ) {
        val subProcessBuilder = ProcessBuilder(listOf(name) + args)
        subProcessBuilder.environment()?.putAll(environment)

        val subProcess = subProcessBuilder.start()
        val subInput = subProcess.inputStream
        val subOutput = subProcess.outputStream
        val subError = subProcess.errorStream

        input.copyTo(subOutput)
        subProcess.waitFor()
        subInput.copyTo(output)
        subError.copyTo(error)
    }
}