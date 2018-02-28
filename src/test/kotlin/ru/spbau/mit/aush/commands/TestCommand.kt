package ru.spbau.mit.aush.commands

import ru.spbau.mit.aush.Command
import ru.spbau.mit.aush.Environment
import ru.spbau.mit.aush.EnvironmentIO
import ru.spbau.mit.aush.EnvironmentVariables
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

abstract class TestCommand(name: String) {
    private val command = Command.getCommand(name)

    private fun runTest(
            arguments: List<String>,
            environment: Environment,
            expectedOutput: String,
            expectedErrorOutput: String = ""
    ) {
        command.evaluate(arguments, environment)
        assertEquals(expectedOutput, environment.io.output.toString())
        assertEquals(expectedErrorOutput, environment.io.error.toString())
    }

    fun runTest(
            arguments: List<String>,
            expectedOutput: String,
            input: String = "",
            expectedErrorOutput: String = ""
    ) = runTest(
            arguments,
            Environment(
                    EnvironmentVariables.emptyVariables,
                    EnvironmentIO(
                            input.byteInputStream(),
                            ByteArrayOutputStream(),
                            ByteArrayOutputStream()
                    )
            ),
            expectedOutput,
            expectedErrorOutput
    )
}