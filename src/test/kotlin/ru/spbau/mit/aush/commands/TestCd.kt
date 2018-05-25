package ru.spbau.mit.aush.commands

import ru.spbau.mit.aush.evaluation.NotDirectoryException
import ru.spbau.mit.aush.evaluation.TooManyArgumentsException
import java.nio.file.Paths
import kotlin.test.Test

class TestCd : TestCommand("cd") {

    @Test
    fun noArgumentTest() {
        runTest(
                emptyList(),
                expectedOutput = ""
        )
    }

    @Test
    fun directoryTest() {
        val expectedDirectory = Paths.get(System.getProperty("user.dir")).resolve("src")

        runTest(
                listOf("src"),
                expectedOutput = "",
                expectedDir = expectedDirectory
        )
    }

    @Test
    fun absolutePathTest() {
        val expectedDirectory = Paths.get(System.getProperty("user.dir")).resolve("src")
        runTest(
                listOf(expectedDirectory.toAbsolutePath().toString()),
                expectedOutput = "",
                expectedDir = expectedDirectory
        )
    }

    @Test(expected = NotDirectoryException::class)
    fun fileTest() {
        runTest(
                listOf("build.gradle"),
                expectedOutput = ""
        )
    }

    @Test(expected = TooManyArgumentsException::class)
    fun multipleArgumentsTest() {
        runTest(
                listOf("arg1", "arg2"),
                expectedOutput = ""
        )
    }
}