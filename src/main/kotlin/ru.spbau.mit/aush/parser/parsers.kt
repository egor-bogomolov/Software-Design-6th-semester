package ru.spbau.mit.aush.parser

import ru.spbau.mit.aush.ast.*
import ru.spbau.mit.aush.lexer.Command
import ru.spbau.mit.aush.lexer.Word
import ru.spbau.mit.aush.lexer.WordPart

/**
 * Represents a parser from F to T
 */
sealed class Parser<in F, out T> {
    /**
     * Parses input
     *
     * @param input input to parse
     * @return parsed result
     */
    abstract fun parse(input: F): T
}

/**
 * Represents a parser of environment variable definitions
 */
private object EnvironmentVariableParser : Parser<Word, Pair<String,Word>?>() {
    /**
     * Parses input
     *
     * @param input {@link ru.spbau.mit.aush.lexer.Word} to parse
     * @return if input is of form `identifier=word` parser succeeds and returns (identifier, word). null otherwise
     */
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

/**
 * Represents a parser of a single command
 */
private object CommandParser : Parser<Command, ASTNode>() {
    /**
     * Parses input. Assumed command form is `envVar* commandName arg*`
     *
     * @param input {@link ru.spbau.mit.aush.lexer.Command} to parse
     * @return ASTNode representing the parsed command
     */
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

/**
 * Represents a parser for input after lexing
 */
object CommandListParser : Parser<List<Command>, ASTNode>() {
    /**
     * Parser list of commands and converts it to AST.
     * Commands are piped in order they are in input list
     *
     * @param input list of lexed commands
     * @return AST representing input
     */
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