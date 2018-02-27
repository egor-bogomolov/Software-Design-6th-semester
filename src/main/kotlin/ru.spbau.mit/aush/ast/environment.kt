package ru.spbau.mit.aush.ast

import java.io.InputStream
import java.io.OutputStream

data class EnvironmentVariables(
        val data: Map<String, String>
) {
    operator fun get(variable: String): String =
            data.getOrDefault(variable, "")

    operator fun plus(
            variableWithValue: Pair<String,String>
    ): EnvironmentVariables =
            EnvironmentVariables(data + variableWithValue)

    operator fun plus(
            environmentVariables: EnvironmentVariables
    ): EnvironmentVariables =
            EnvironmentVariables(
                    data + environmentVariables.data
            )

    companion object {
        val emptyVariables = EnvironmentVariables(emptyMap())
    }
}

data class EnvironmentIO(
        val input: InputStream,
        val output: OutputStream,
        val error: OutputStream
)

data class Environment(
        val variables: EnvironmentVariables,
        val io: EnvironmentIO
)