package ru.spbau.mit.aush.evaluation

import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Represents environment variables
 * Why not simply Map<String, String>?
 * Because the way of representation can change somewhere in the future
 */
data class EnvironmentVariables(
        val data: Map<String, String>
) {
    /**
     * []-operator convenience method
     * Gets variable value from environment
     *
     * @param variable variable name to get
     * @return variable value if it is defined and empty string otherwise
     */
    operator fun get(variable: String): String =
            data.getOrDefault(variable, "")

    /**
     * +-operator convenience method
     * Adds variable to environment
     *
     * @param variableWithValue variable to add
     * @return new EnvironmentVariables containing added variable
     */
    operator fun plus(
            variableWithValue: Pair<String,String>
    ): EnvironmentVariables =
            EnvironmentVariables(data + variableWithValue)

    /**
     * +-operator convenience method
     * Adds all variables from another environment (or redefines if any are present)
     *
     * @param environmentVariables environment to add
     * @return new EnvironmentVariables containing variables from both
     */
    operator fun plus(
            environmentVariables: EnvironmentVariables
    ): EnvironmentVariables =
            EnvironmentVariables(
                    data + environmentVariables.data
            )

    companion object {
        // predefined empty environment
        val emptyVariables = EnvironmentVariables(emptyMap())
    }
}

/**
 * Represents environment I/O interface
 */
data class EnvironmentIO(
        val input: InputStream,
        val output: OutputStream,
        val error: OutputStream
)

/**
 * Represents environment (variables + I/O)
 */
data class Environment(
        val variables: EnvironmentVariables,
        val io: EnvironmentIO,
        var currentDir: Path
)