package ru.spbau.mit.aush.evaluation.commands

import ru.spbau.mit.aush.evaluation.Environment
import java.io.PrintStream

/**
 * Represents simplified version of `pwd` Bash command
 * Outputs current working directory
 */
internal object PwdCommand : Command() {

    /**
     * {@inheritDoc}
     */
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {
        PrintStream(environment.io.output).println(System.getProperty("user.dir"))
    }
}