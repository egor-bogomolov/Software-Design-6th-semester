package ru.spbau.mit.aush.commands

import ru.spbau.mit.aush.evaluation.Environment
import ru.spbau.mit.aush.evaluation.EnvironmentIO
import ru.spbau.mit.aush.evaluation.EnvironmentVariables
import ru.spbau.mit.aush.evaluation.commands.Command
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertEquals

abstract class TestCommand(name: String) {
    private val command = Command.getCommand(name)

    private fun runTest(
            arguments: List<String>,
            environment: Environment,
            expectedOutput: String,
            expectedErrorOutput: String = "",
            expectedDir: Path
    ) {
        command.evaluate(arguments, environment)
        assertEquals(expectedOutput, environment.io.output.toString())
        assertEquals(expectedErrorOutput, environment.io.error.toString())
        assertEquals(expectedDir, environment.currentDir)
    }

    fun runTest(
            arguments: List<String>,
            expectedOutput: String,
            input: String = "",
            expectedErrorOutput: String = "",
            currentDir: Path = Paths.get(System.getProperty("user.dir")),
            expectedDir: Path = Paths.get(System.getProperty("user.dir"))
    ) = runTest(
            arguments,
            Environment(
                    EnvironmentVariables.emptyVariables,
                    EnvironmentIO(
                            input.byteInputStream(),
                            ByteArrayOutputStream(),
                            ByteArrayOutputStream()
                    ),
                    currentDir
            ),
            expectedOutput,
            expectedErrorOutput,
            expectedDir
    )
}