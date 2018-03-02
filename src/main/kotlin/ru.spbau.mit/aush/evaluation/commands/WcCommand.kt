package ru.spbau.mit.aush.evaluation.commands

import ru.spbau.mit.aush.evaluation.Environment
import java.io.File
import java.io.InputStream
import java.io.PrintStream

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

/**
 * Represents simplified version of `wc` Bash command
 * For each of its arguments outputs number of lines, words and bytes in a corresponding file
 * Also outputs total counts
 * If no arguments are given then outputs same statistics for its input stream
 */
internal object WcCommand : Command() {
    /**
     * {@inheritDoc}
     */
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