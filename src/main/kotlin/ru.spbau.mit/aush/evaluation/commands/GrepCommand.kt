package ru.spbau.mit.aush.evaluation.commands

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import ru.spbau.mit.aush.evaluation.Environment
import java.io.File
import java.io.PrintStream

/**
 * Storage class for grep arguments
 */
private class GrepArgs(parser: ArgParser) {
    val ignoreCase by parser.flagging("-i", help = "ignore case")

    val wholeWords by parser.flagging("-w", help = "match only whole words")

    val linesAfterMatch by parser.storing(
            "-A",
            help = "number of lines to output after each match"
    ) { toInt() }.default(0)

    val regex by parser.positional("REGEX", help = "regular expression to search for")

    val files by parser.positionalList("FILE", help = "file to search in")
            .default(emptyList())
}

/**
 * Represents simplified version of `grep` Bash command
 * Searches for regexp match in files given in arguments by their file names
 * (if present, in input stream otherwise)
 * and outputs lines with matches
 * Supported arguments:
 * - -i -- ignore case
 * - -w -- match only if regexp matches the whole word
 * - -A n -- for each match output additional n lines following the matched line
 */
internal object GrepCommand : Command() {

    /**
     * {@inheritDoc}
     */
    override fun evaluate(args: List<String>, environment: Environment) {
        val parsedArgs = ArgParser(args.toTypedArray()).parseInto(::GrepArgs)

        val regex = Regex(
                if (parsedArgs.wholeWords) {
                    """\b${parsedArgs.regex}\b"""
                } else {
                    parsedArgs.regex
                },
                if (parsedArgs.ignoreCase) {
                    setOf(RegexOption.IGNORE_CASE)
                } else {
                    emptySet()
                }
        )

        val streamsToSearch = if (parsedArgs.files.isEmpty()) {
            listOf(Pair("", environment.io.input))
        } else {
            parsedArgs.files.map {
                val file = environment.currentDir.resolve(it).toFile()
                Pair(file.name + ":", file.inputStream())
            }
        }

        val output = PrintStream(environment.io.output)

        for ((prefix, stream) in streamsToSearch) {
            var linesToOutput = 0
            for (line in stream.bufferedReader().readLines()) {
                if (regex.containsMatchIn(line)) {
                    linesToOutput = 1 + parsedArgs.linesAfterMatch
                }
                if (linesToOutput > 0) {
                    output.println(prefix + line)
                    linesToOutput--
                }
            }
        }
    }
}
