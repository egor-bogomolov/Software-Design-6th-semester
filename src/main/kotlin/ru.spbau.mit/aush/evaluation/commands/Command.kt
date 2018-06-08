package ru.spbau.mit.aush.evaluation.commands

import ru.spbau.mit.aush.evaluation.Environment

/**
 * Represents shell command
 */
abstract class Command {
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
        fun getCommand(name: String): Command = builtInCommands[name] ?: ExternalCommand(name)

        private val builtInCommands = mapOf(
                "cat" to CatCommand,
                "echo" to EchoCommand,
                "wc" to WcCommand,
                "pwd" to PwdCommand,
                "exit" to ExitCommand,
                "grep" to GrepCommand,
                "cd" to CdCommand,
                "ls" to LsCommand
        )
    }
}