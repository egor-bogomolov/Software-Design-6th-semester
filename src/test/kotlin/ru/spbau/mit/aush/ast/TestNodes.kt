package ru.spbau.mit.aush.ast

import ru.spbau.mit.aush.evaluation.Environment
import ru.spbau.mit.aush.evaluation.EnvironmentIO
import ru.spbau.mit.aush.evaluation.EnvironmentVariables
import ru.spbau.mit.aush.lexer.Word
import ru.spbau.mit.aush.lexer.WordPart
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class TestNodes {
    private val echoCommandNode =
            CommandNode(
                    Word(listOf(
                            WordPart("echo", WordPart.Type.UNQUOTED)
                    )),
                    listOf(
                            Word(listOf(
                                    WordPart("first", WordPart.Type.UNQUOTED)
                            )),
                            Word(listOf(
                                    WordPart("second", WordPart.Type.UNQUOTED)
                            ))
                    )
            )

    private val catCommandNode =
            CommandNode(
                    Word(listOf(
                            WordPart("cat", WordPart.Type.UNQUOTED)
                    )),
                    emptyList()
            )

    private fun runTest(
            astNode: ASTNode,
            environment: Environment,
            expectedOutput: String,
            expectedErrorOutput: String = ""
    ) {
        astNode.evaluate(environment)
        assertEquals(expectedOutput, environment.io.output.toString())
        assertEquals(expectedErrorOutput, environment.io.error.toString())
    }

    private fun runTest(
            astNode: ASTNode,
            expectedOutput: String,
            input: String = "",
            expectedErrorOutput: String = ""
    ) = runTest(
            astNode,
            Environment(
                    EnvironmentVariables.emptyVariables,
                    EnvironmentIO(
                            input.byteInputStream(),
                            ByteArrayOutputStream(),
                            ByteArrayOutputStream()
                    )
            ),
            expectedOutput,
            expectedErrorOutput
    )

    @Test
    fun commandNodeOutputTest() {
        runTest(
                echoCommandNode,
                "first second\n"
        )
    }

    @Test
    fun commandNodeInputTest() {
        val testInput = "test input"

        runTest(
                catCommandNode,
                testInput,
                testInput
        )
    }

    @Test
    fun pipeNodeTest() {
        val pipeNode = PipeNode(echoCommandNode, catCommandNode)

        runTest(
                pipeNode,
                "first second\n"
        )
    }

    @Test
    fun defineVariableNodeTest() {
        val echoVariableValueCommandNode =
                CommandNode(
                        Word(listOf(
                                WordPart("echo", WordPart.Type.UNQUOTED)
                        )),
                        listOf(
                                Word(listOf(
                                        WordPart("\$var", WordPart.Type.UNQUOTED)
                                ))
                        )
                )

        runTest(
                echoVariableValueCommandNode,
                "\n"
        )

        val defineVariableNode =
                DefineVariableNode(
                        "var",
                        Word(listOf(
                                WordPart("test", WordPart.Type.UNQUOTED)
                        )),
                        echoVariableValueCommandNode
                )

        runTest(
                defineVariableNode,
                "test\n"
        )
    }
}