package ru.spbau.mit.aush.commands

import java.io.File
import kotlin.test.Test

class TestGrep : TestCommand("grep") {
    @Test
    fun simpleTest() {
        val regex = "joll"

        val filePath = javaClass.getResource("/grepTest.txt").path

        val expectedOutput =
                javaClass.getResource("/grepTestCorrectOutput.txt").readText()

        runTest(
                listOf(regex, filePath),
                expectedOutput,
                ""
        )
    }

    @Test
    fun wholeWordTest() {
        val regex = "joll"

        val filePath = javaClass.getResource("/grepTest.txt").path

        val expectedOutput = ""

        runTest(
                listOf(regex, filePath, "-w"),
                expectedOutput,
                ""
        )
    }

    @Test
    fun ignoreCaseTest() {
        val regex = "joll"

        val filePath = javaClass.getResource("/grepTest.txt").path

        val expectedOutput =
                javaClass.getResource("/grepTestIgnoreCaseCorrectOutput.txt").readText()

        runTest(
                listOf(regex, filePath, "-i"),
                expectedOutput,
                ""
        )
    }

    @Test
    fun multipleArgumentsTest() {
        val regex = "joll"

        val filePath = javaClass.getResource("/grepTest.txt").path

        val expectedOutput =
                javaClass.getResource("/grepTestCorrectOutput.txt").readText()

        runTest(
                listOf(regex, filePath, filePath),
                expectedOutput.repeat(2),
                ""
        )
    }

    @Test
    fun zeroArgumentsTest() {
        val regex = "joll"

        val filePath = javaClass.getResource("/grepTest.txt").path

        val expectedOutput =
                javaClass.getResource("/grepTestCorrectOutput.txt").readText()


        runTest(
                listOf(regex),
                expectedOutput
                        .lines()
                        .joinToString("\n") {
                    it.removePrefix("grepTest.txt:")
                },
                File(filePath).readText()
        )
    }
}