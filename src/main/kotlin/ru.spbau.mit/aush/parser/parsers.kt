package ru.spbau.mit.aush.parser

import ru.spbau.mit.aush.lexer.Command
import ru.spbau.mit.aush.lexer.Word
import ru.spbau.mit.aush.lexer.WordPart
import ru.spbau.mit.aush.repl.ReplState

sealed class Parser<in F, out T> {
    abstract fun parse(input: F): T
}

object EnvironmentVariableParser : Parser<Word, Pair<String,Word>?>() {
    override fun parse(input: Word): Pair<String,Word>? {
        val firstPart = input.parts.first()
        if (firstPart.type == WordPart.Type.UNQUOTED) {
            val assignIndex = firstPart.string.indexOf('=')
            if (assignIndex != -1
                    && firstPart
                            .string
                            .first()
                            .isJavaIdentifierStart()
                    && firstPart
                            .string
                            .substring(0..assignIndex)
                            .all(Char::isJavaIdentifierPart)) {
                val name = firstPart.string.substring(0..assignIndex)
                val newFirstPart = firstPart.string.substring(assignIndex + 1)
                return name to Word(
                        listOf(WordPart(newFirstPart, WordPart.Type.UNQUOTED))
                                + input.parts.drop(1))
            }
        }
        return null
    }
}

sealed class CommandParser(private val isSingle: Boolean) : Parser<Command, ASTNode>() {
    override fun parse(input: Command): ASTNode {
        val commandNameIndex =
                input.words.indexOfFirst {
                    EnvironmentVariableParser.parse(it) == null
                }
        val environmentVariables =
                input
                        .words
                        .take(commandNameIndex)
                        .map { EnvironmentVariableParser.parse(it)!! }
        val tailNode: ASTNode = when {
            isSingle && commandNameIndex == -1 -> ReplState.SubmitVariablesNode
            commandNameIndex == -1 -> EmptyNode
            else -> CommandNode(
                    input.words[commandNameIndex],
                    input.words.drop(commandNameIndex + 1)
            )
        }
        return environmentVariables.foldRight(
                tailNode,
                { variable, node -> DefineVariableNode(variable, node) }
        )
    }
}

object PipedCommandParser : CommandParser(false)
object SingleCommandParser : CommandParser(true)

object CommandListParser : Parser<List<Command>, ASTNode>() {
    override fun parse(input: List<Command>): ASTNode {
        return when (input.count()) {
            0 -> EmptyNode
            1 -> SingleCommandParser.parse(input.first())
            else -> input
                    .map { PipedCommandParser.parse(it) }
                    .reduceRight { command, tail -> PipeNode(command, tail) }
        }
    }
}