package ru.spbau.mit.aush.parser

import ru.spbau.mit.aush.ast.CommandNode
import ru.spbau.mit.aush.ast.DefineVariableNode
import ru.spbau.mit.aush.ast.EmptyNode
import ru.spbau.mit.aush.lexer.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCommandParser {
    @Test
    fun simpleTest() {
        val commands = (Lexer.tryLex(
                """
                    a=3 echo ${"$"}a
                    |a=3 b=4 c=5
                    |pwd
                    """
        ) as LexSuccess).commands

        val expectedNodes = listOf(
                DefineVariableNode(
                        "a",
                        Word(listOf(
                                WordPart("3", WordPart.Type.UNQUOTED)
                        )),
                        CommandNode(
                                Word(listOf(
                                        WordPart("echo", WordPart.Type.UNQUOTED)
                                )),
                                listOf(
                                        Word(listOf(
                                                WordPart("\$a", WordPart.Type.UNQUOTED)
                                        ))
                                )
                        )
                ),
                DefineVariableNode(
                        "a",
                        Word(listOf(
                                WordPart("3", WordPart.Type.UNQUOTED)
                        )),
                        DefineVariableNode(
                                "b",
                                Word(listOf(
                                        WordPart("4", WordPart.Type.UNQUOTED)
                                )),
                                DefineVariableNode(
                                        "c",
                                        Word(listOf(
                                                WordPart("5", WordPart.Type.UNQUOTED)
                                        )),
                                        EmptyNode
                                )
                        )
                ),
                CommandNode(
                        Word(listOf(
                                WordPart("pwd", WordPart.Type.UNQUOTED)
                        )),
                        listOf()
                )
        )

        expectedNodes
                .zip(commands)
                .forEach {
                    assertEquals(it.first, CommandParser.parse(it.second))
                }

        assertEquals(EmptyNode, CommandParser.parse(LCommand(emptyList())))
    }
}