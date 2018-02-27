package ru.spbau.mit.aush.parser

import ru.spbau.mit.aush.lexer.Word
import ru.spbau.mit.aush.lexer.WordPart
import ru.spbau.mit.aush.repl.Command
import ru.spbau.mit.aush.repl.Environment
import java.io.PipedInputStream
import java.io.PipedOutputStream

private fun Word.interpolate(environment: Environment): String =
        parts.joinToString {
            when (it.type) {
                WordPart.Type.PLAIN_QUOTED, WordPart.Type.UNQUOTED ->
                    "\$\\w".toRegex().replace(it.string) {
                        match -> environment[match.value.substring(1)]
                    }
                WordPart.Type.RAW_QUOTED -> it.string
            }
        }

abstract class ASTNode {
    abstract fun evaluate(
            environment: Environment
    )
}

class CommandNode(
        private val commandName: Word,
        private val args: List<Word>
) : ASTNode() {
    override fun evaluate(environment: Environment) {
        Command.getCommand(commandName.interpolate(environment)).evaluate(
                args.map { it.interpolate(environment) },
                environment
        )
    }
}

class PipeNode(
        private val left: ASTNode,
        private val right: ASTNode
) : ASTNode() {
    override fun evaluate(environment: Environment) {
        val pipeIn = PipedInputStream()
        val pipeOut = PipedOutputStream()
        pipeIn.connect(pipeOut)
        left.evaluate(environment.copy(output = pipeOut))
        right.evaluate(environment.copy(input = pipeIn))
    }
}

class DefineVariableNode(
        private val name: String,
        private val value: Word,
        private val followingNode: ASTNode
) : ASTNode() {
    constructor(nameValuePair: Pair<String, Word>, followingNode: ASTNode) :
            this(nameValuePair.first, nameValuePair.second, followingNode)

    override fun evaluate(environment: Environment) = followingNode.evaluate(
            environment.copy(
                    variables = environment.variables
                            + (name to value.interpolate(environment))
            )
    )
}

object EmptyNode : ASTNode() {
    override fun evaluate(environment: Environment) {
        // do nothing
    }
}