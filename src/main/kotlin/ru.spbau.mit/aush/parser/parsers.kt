package ru.spbau.mit.aush.parser

import ru.spbau.mit.aush.ast.*
import ru.spbau.mit.aush.lexer.Command
import ru.spbau.mit.aush.lexer.Word
import ru.spbau.mit.aush.lexer.WordPart

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

object CommandParser : Parser<Command, ASTNode>() {
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
        val tailNode: ASTNode = when (commandNameIndex) {
            -1 -> EmptyNode
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

object CommandListParser : Parser<List<Command>, ASTNode>() {
    override fun parse(input: List<Command>): ASTNode {
        return if (input.isEmpty()) {
            EmptyNode
        } else {
            input
                    .map { CommandParser.parse(it) }
                    .reduceRight { command, tail -> PipeNode(command, tail) }
        }
    }
}