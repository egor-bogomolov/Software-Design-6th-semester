package ru.spbau.mit.aush.commands

import kotlin.test.Test

class TestWc : TestCommand("wc") {
    @Test
    fun noArgumentsTest() {
        val testInput = "test input"

        runTest(
                emptyList(),
                "\t1\t2\t10\n",
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
                "\t1\t2\t13\t${file.absolutePath}\n",
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
                """
                |	1	3	19	${firstFile.absolutePath}
	            |	1	3	20	${secondFile.absolutePath}
	            |	2	6	39	total
                |""".trimMargin(),
                testInput
        )
    }
}