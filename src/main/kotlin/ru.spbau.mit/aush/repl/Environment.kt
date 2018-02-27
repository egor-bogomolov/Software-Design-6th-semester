package ru.spbau.mit.aush.repl

import java.io.InputStream
import java.io.OutputStream

data class Environment(
        val variables: Map<String, String>,
        val input: InputStream,
        val output: OutputStream,
        val error: OutputStream) {
    operator fun get(variable: String): String =
            variables.getOrDefault(variable, "")
}