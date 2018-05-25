package ru.spbau.mit.aush.commands

import java.nio.file.Paths
import kotlin.test.Test

class TestLs : TestCommand("ls") {

    private val dirName1 = "lsTestDirectory1"
    private val dirName2 = "lsTestDirectory2"
    private val pathToRoot = Paths.get(javaClass.getResource("/lsRoot").path)
    private val pathToDirectory1 = pathToRoot.resolve(dirName1)
    private val pathToDirectory2 = pathToRoot.resolve(dirName2)
    private val content = "file1.txt file2.txt file3.txt\n"

    @Test
    fun singleDirectoryAbsoluteTest() {
        runTest(
                listOf(pathToDirectory1.toString()),
                expectedOutput = content,
                currentDir = pathToRoot,
                expectedDir = pathToRoot
        )
    }

    @Test
    fun singleDirectoryRelativeTest() {
        runTest(
                listOf(pathToRoot.relativize(pathToDirectory1).toString()),
                expectedOutput = content,
                currentDir = pathToRoot,
                expectedDir = pathToRoot
        )
    }

    @Test
    fun multipleDirectoryTest() {
        runTest(
                listOf(pathToDirectory1.toString(), pathToDirectory2.toString()),
                expectedOutput = "\n$dirName1:\n$content\n$dirName2:\n$content",
                currentDir = pathToRoot,
                expectedDir = pathToRoot
        )
    }

    @Test
    fun noArgumentsTest() {
        runTest(
                emptyList(),
                expectedOutput = content,
                currentDir = pathToDirectory1,
                expectedDir = pathToDirectory1
        )
    }

    @Test
    fun fileTest() {
        runTest(
                listOf(
                        pathToDirectory1.resolve("file1.txt").toString(),
                        pathToDirectory1.resolve("file2.txt").toString()
                ),
                expectedOutput = "file1.txt file2.txt\n",
                currentDir = pathToRoot,
                expectedDir = pathToRoot
        )
    }

}