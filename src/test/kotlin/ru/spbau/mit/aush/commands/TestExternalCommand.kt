package ru.spbau.mit.aush.commands

import org.junit.Assume
import java.io.File
import kotlin.test.Test

class TestExternalCommand : TestCommand("dirname") {
    @Test
    fun simpleTest() {
        Assume.assumeFalse(
                System
                        .getProperty("os.name")
                        .toLowerCase()
                        .startsWith("win")
        )

        val directory = File(System.getProperty("user.dir"))

        runTest(
                listOf(directory.toString()),
                directory.parent.toString() + "\n"
        )
    }
}