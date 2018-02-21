package ru.spbau.mit.aush

import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

abstract class ASTNode {
    abstract fun evaluate(
            input : InputStream = System.`in`,
            output : OutputStream = System.out,
            error : OutputStream = System.err
    )
}

class CommandNode(val command: Command, val args: List<String>) : ASTNode() {
    override fun evaluate(
            input: InputStream,
            output: OutputStream,
            error: OutputStream
    ) = command.evaluate(args, input, output, error)
}

class PipeNode(val left: ASTNode, val right: ASTNode) : ASTNode() {
    override fun evaluate(
            input: InputStream,
            output: OutputStream,
            error: OutputStream
    ) {
        val pipeIn = PipedInputStream()
        val pipeOut = PipedOutputStream()
        pipeIn.connect(pipeOut)
        left.evaluate(input, pipeOut, error)
        right.evaluate(pipeIn, output, error)
    }
}