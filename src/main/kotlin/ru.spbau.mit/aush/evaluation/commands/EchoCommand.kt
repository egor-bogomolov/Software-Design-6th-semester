package ru.spbau.mit.aush.evaluation.commands

import ru.spbau.mit.aush.evaluation.Environment
import java.io.PrintStream

/**
 * Represents simplified version of `echo` Bash command
 * Outputs its arguments separated by whitespace character on a new line
 */
internal object EchoCommand : Command() {

    /**
     * {@inheritDoc}
     */
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        PrintStream(environment.io.output).println(
                args.joinToString(" ")
        )
    }
}