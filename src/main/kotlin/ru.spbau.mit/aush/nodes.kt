package ru.spbau.mit.aush

import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class TokenString(private val string: String, private val type: TokenType) {
    enum class TokenType {
        PLAIN,
        RAW
    }

    fun interpolate(environment: Map<String,String>): String =
            when (type) {
                TokenType.PLAIN -> "\$\\w".toRegex().replace(string) {
                    match -> environment.getOrDefault(match.value.substring(1), "")
                }
                TokenType.RAW -> string
            }
}

abstract class ASTNode {
    abstract fun evaluate(
            input: InputStream = System.`in`,
            output: OutputStream = System.out,
            error: OutputStream = System.err,
            environment: MutableMap<String, String> = mutableMapOf()
    )
}

class CommandNode(
        private val commandName: TokenString,
        private val args: List<TokenString>
) : ASTNode() {
    override fun evaluate(
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: MutableMap<String, String>
    ) {
        Command.getCommand(commandName.interpolate(environment)).evaluate(
                args.map { it.interpolate(environment) },
                input,
                output,
                error,
                environment
        )
    }
}

class PipeNode(
        private val left: ASTNode,
        private val right: ASTNode
) : ASTNode() {
    override fun evaluate(
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: MutableMap<String, String>
    ) {
        val pipeIn = PipedInputStream()
        val pipeOut = PipedOutputStream()
        pipeIn.connect(pipeOut)
        left.evaluate(input, pipeOut, error, environment)
        right.evaluate(pipeIn, output, error, environment)
    }
}

class DefineLocalVariableNode(
        private val name: String,
        private val value: TokenString,
        private val followingNode: ASTNode
) : ASTNode() {
    constructor(nameValuePair: Pair<String,TokenString>, followingNode: ASTNode) :
            this(nameValuePair.first, nameValuePair.second, followingNode)

    override fun evaluate(
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: MutableMap<String, String>
    ) = followingNode.evaluate(
            input,
            output,
            error,
            (environment + (name to value.interpolate(environment))).toMutableMap()
    )
}

class DefineGlobalVariableNode(
        private val name: String,
        private val value: TokenString,
        private val followingNode: ASTNode
) : ASTNode() {
    constructor(nameValuePair: Pair<String,TokenString>, followingNode: ASTNode) :
            this(nameValuePair.first, nameValuePair.second, followingNode)

    override fun evaluate(
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: MutableMap<String, String>
    ) {
        environment += (name to value.interpolate(environment))
        followingNode.evaluate(
                input,
                output,
                error,
                environment
        )
    }
}

object EmptyNode : ASTNode() {
    override fun evaluate(
            input: InputStream,
            output: OutputStream,
            error: OutputStream,
            environment: MutableMap<String, String>
    ) {
        // do nothing
    }
}