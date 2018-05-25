package ru.spbau.mit.aush.evaluation.commands

import ru.spbau.mit.aush.evaluation.Environment

/**
 * Represents simplified version of `exit` Bash command
 * Exits the shell
 */
internal object ExitCommand : Command() {

    /**
     * {@inheritDoc}
     */
    override fun evaluate(
            args: List<String>,
            environment: Environment
    ) {

    }
}
