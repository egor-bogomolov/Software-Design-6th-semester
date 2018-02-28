package ru.spbau.mit.aush.lexer

import kotlin.test.Test
import kotlin.test.assertEquals

class TestLexer {
    @Test
    fun testLexWord() {
        val input = """prefix"double quoted"middle'single quoted'suffix"""
        val expectedWord = Word(
                listOf(
                        WordPart("prefix", WordPart.Type.UNQUOTED),
                        WordPart("double quoted", WordPart.Type.PLAIN_QUOTED),
                        WordPart("middle", WordPart.Type.UNQUOTED),
                        WordPart("single quoted", WordPart.Type.RAW_QUOTED),
                        WordPart("suffix", WordPart.Type.UNQUOTED)
                )
        )

        val result = Lexer.tryLex(input)
        assertEquals(
                LexSuccess(
                        listOf(
                                Command(
                                        listOf(
                                                expectedWord
                                        )
                                )
                        )
                ),
                result
        )
    }

    @Test
    fun testLexMargins() {
        val input = """    first
            second  """
        val expectedWords = listOf(
                Word(listOf(
                        WordPart("first", WordPart.Type.UNQUOTED)
                )),
                Word(listOf(
                        WordPart("second", WordPart.Type.UNQUOTED)
                ))
        )

        val result = Lexer.tryLex(input)
        assertEquals(
                LexSuccess(
                        listOf(
                                Command(
                                        expectedWords
                                )
                        )
                ),
                result
        )
    }

    @Test
    fun testMissingPairSingleQuote() {
        val input = """'first
            """

        val result = Lexer.tryLex(input)
        assert(result is LexFailure
                && result.exception is UnclosedQuote
                && result.exception.message == "missing pair to a '''-quote"
        )
    }


    @Test
    fun testMissingPairDoubleQuote() {
        val input = """first "
            """

        val result = Lexer.tryLex(input)
        assert(result is LexFailure
                && result.exception is UnclosedQuote
                && result.exception.message == "missing pair to a '\"'-quote"
        )
    }

    @Test
    fun testLexPipe() {
        val input = """first |
            second"""

        val result = Lexer.tryLex(input)
        assertEquals(
                LexSuccess(listOf(
                        Command(listOf(
                                Word(listOf(
                                        WordPart("first", WordPart.Type.UNQUOTED)
                                ))
                        )),
                        Command(listOf(
                                Word(listOf(
                                        WordPart("second", WordPart.Type.UNQUOTED)
                                ))
                        ))
                )),
                result
        )
    }

    @Test
    fun testLexNoCommandBeforePipe() {
        val input = """ |
            second"""

        val result = Lexer.tryLex(input)
        assertEquals(
                LexFailure(
                        NoCommandBeforePipe
                ),
                result
        )
    }

    @Test
    fun testLexNoCommandAfterPipe() {
        val input = """first |
            """

        val result = Lexer.tryLex(input)
        assertEquals(
                LexFailure(
                        NoCommandAfterPipe
                ),
                result
        )
    }
}