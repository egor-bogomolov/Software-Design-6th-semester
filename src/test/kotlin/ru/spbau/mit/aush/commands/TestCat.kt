package ru.spbau.mit.aush.commands

import kotlin.test.Test

class TestCat : TestCommand("cat") {
    @Test
    fun noArgumentsTest() {
        val testInput = "test input"

        runTest(
                emptyList(),
                testInput,
                testInput
        )
    }

    @Test
    fun singleArgumentTest() {
        val testInput = "test input"
        val fileContents = "file contents"

        val file = createTempFile()
        file.appendText(fileContents)

        runTest(
                listOf(file.absolutePath),
                "$fileContents\n",
                testInput
        )
    }

    @Test
    fun multipleArgumentsTest() {
        val testInput = "test input"

        val firstFileContents = "first file contents"
        val firstFile = createTempFile()
        firstFile.appendText(firstFileContents)

        val secondFileContents = "second file contents"
        val secondFile = createTempFile()
        secondFile.appendText(secondFileContents)

        runTest(
                listOf(firstFile.absolutePath, secondFile.absolutePath),
                "$firstFileContents\n$secondFileContents\n",
                testInput
        )
    }
}