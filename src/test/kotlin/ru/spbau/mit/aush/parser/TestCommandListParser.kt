package ru.spbau.mit.aush.parser

import ru.spbau.mit.aush.ast.CommandNode
import ru.spbau.mit.aush.ast.DefineVariableNode
import ru.spbau.mit.aush.ast.EmptyNode
import ru.spbau.mit.aush.ast.PipeNode
import ru.spbau.mit.aush.lexer.LexSuccess
import ru.spbau.mit.aush.lexer.Lexer
import ru.spbau.mit.aush.lexer.Word
import ru.spbau.mit.aush.lexer.WordPart
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCommandListParser {
    @Test
    fun simpleTest() {
        val commands = (Lexer.tryLex(
                """
                    a=3 echo ${"$"}a
                    |a=3 b=4 c=5
                    |pwd
                    """
        ) as LexSuccess).commands

        val expectedNode = listOf(
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
        ).reduceRight { left, right ->
            PipeNode(left, right)
        }

        assertEquals(expectedNode, CommandListParser.parse(commands))
    }
}