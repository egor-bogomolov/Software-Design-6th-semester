package ru.spbau.mit.aush.commands

import kotlin.test.Test

class TestPwd : TestCommand("pwd") {
    @Test
    fun simpleTest() {
        val ignoredInput = "ignored"

        runTest(
                listOf(ignoredInput),
                System.getProperty("user.dir") + "\n",
                ignoredInput
        )
    }

}
