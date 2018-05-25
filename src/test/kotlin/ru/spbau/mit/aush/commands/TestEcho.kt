package ru.spbau.mit.aush.commands

import kotlin.test.Test

class TestEcho : TestCommand("echo") {
    @Test
    fun noArgumentTest() {
        val testInput = "test input"

        runTest(
                emptyList(),
                "\n",
                testInput
        )
    }

    @Test
    fun singleArgumentTest() {
        val testInput = "test input"

        runTest(
                listOf("first"),
                "first\n",
                testInput
        )
    }

    @Test
    fun multipleArgumentTest() {
        val testInput = "test input"

        runTest(
                listOf("first", "second"),
                "first second\n",
                testInput
        )
    }
}