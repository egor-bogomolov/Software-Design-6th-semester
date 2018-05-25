package ru.spbau.mit.aush.parser

import ru.spbau.mit.aush.lexer.LexSuccess
import ru.spbau.mit.aush.lexer.Lexer
import ru.spbau.mit.aush.lexer.Word
import ru.spbau.mit.aush.lexer.WordPart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestEnvironmentVariableParser {
    @Test
    fun noParseTest() {
        val words = (Lexer.tryLex(
                """
                |1a=3
                |"a"=3
                |'a'=3
                |a=
                """.trimMargin()
        ) as LexSuccess).commands.first().words

        words.forEach {
            assertNull(
                    EnvironmentVariableParser.parse(it)
            )
        }
    }

    @Test
    fun simpleTest() {
        val words = (Lexer.tryLex(
                """
                |a=3
                |a="3"
                |a='3'
                """.trimMargin()
        ) as LexSuccess).commands.first().words

        val expectedVariables =
                listOf(
                        Pair(
                                "a",
                                Word(listOf(
                                        WordPart("3", WordPart.Type.UNQUOTED)
                                ))
                        ),
                        Pair(
                                "a",
                                Word(listOf(
                                        WordPart("", WordPart.Type.UNQUOTED),
                                        WordPart("3", WordPart.Type.PLAIN_QUOTED)
                                ))
                        ),
                        Pair(
                                "a",
                                Word(listOf(
                                        WordPart("", WordPart.Type.UNQUOTED),
                                        WordPart("3", WordPart.Type.RAW_QUOTED)
                                ))
                        )
                )

        expectedVariables
                .zip(words)
                .forEach {
                    assertEquals(
                            it.first,
                            EnvironmentVariableParser.parse(it.second)
                    )
                }
    }
}