package ru.spbau.mit.aush.evaluation.commands

import ru.spbau.mit.aush.evaluation.Environment
import java.io.File
import java.io.PrintStream

/**
 * Represents simplified version of `cat` Bash command
 * Outputs contents of files given in its arguments
 * If no arguments are provided then input stream is outputted
 */
internal object CatCommand : Command() {

    /**
     * {@inheritDoc}
     */
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        if (args.isEmpty()) {
            environment.io.input.copyTo(environment.io.output)
        } else {
            val printStream = PrintStream(environment.io.output)

            for (fileName in args) {
                environment.currentDir.resolve(fileName).toFile().inputStream().copyTo(environment.io.output)
                printStream.println()
            }
        }
    }
}