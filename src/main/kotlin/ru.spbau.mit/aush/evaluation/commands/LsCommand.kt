package ru.spbau.mit.aush.evaluation.commands

import ru.spbau.mit.aush.evaluation.Environment
import ru.spbau.mit.aush.evaluation.FileNotExistException
import java.io.File
import java.io.PrintStream

/**
 * Represents simplified version of `ls` Bash command
 * For each of its arguments prints a list of files in corresponding directory
 * If no arguments provided then it's done for current directory
 */
internal object LsCommand : Command() {

    /**
     * {@inheritDoc}
     */
    override fun evaluate(args: List<String>, environment: Environment) {
        val targets: List<File> =
                if (args.isEmpty()) {
                    listOf(environment.currentDir.toFile())
                } else {
                    args.map { environment.currentDir.resolve(it).toFile() }
                }

        targets.forEach {
            if (!it.exists()) {
                throw FileNotExistException(it)
            }
        }

        val files = targets.filter { it.isFile }
        val directories = targets.filter { it.isDirectory }
        val output = PrintStream(environment.io.output)

        if (files.isNotEmpty()) {
            output.println(files.joinToString(separator = " "){ it.name })
        }

        directories.forEach {
            if (directories.size > 1 || files.isNotEmpty()) {
                output.println()
                output.println("${it.relativeTo(environment.currentDir.toFile())}:")
            }
            output.println(it.listFiles().joinToString(separator = " ") { file -> file.name })
        }
    }

}