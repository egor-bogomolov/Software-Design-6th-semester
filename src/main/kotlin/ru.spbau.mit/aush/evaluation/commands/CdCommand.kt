package ru.spbau.mit.aush.evaluation.commands

import ru.spbau.mit.aush.evaluation.*

/**
 * Represents simplified version of `cd` Bash command
 * Changes current directory to the one provided as argument
 */
internal object CdCommand : Command() {

    /**
     * {@inheritDoc}
     */
    override fun evaluate(args: List<String>, environment: Environment) {
        when(args.size) {
            0 -> {}
            1 ->  {
                val targetDirectory = environment.currentDir.resolve(args[0]).normalize().toFile()
                if (!targetDirectory.exists()) {
                    throw FileNotExistException(targetDirectory)
                }
                if (!targetDirectory.isDirectory) {
                    throw NotDirectoryException(targetDirectory)
                }
                environment.currentDir = targetDirectory.toPath()
            }
            else -> throw TooManyArgumentsException(1, args.size)
        }
    }

}