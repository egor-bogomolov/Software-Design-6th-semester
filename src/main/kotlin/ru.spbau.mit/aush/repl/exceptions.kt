package ru.spbau.mit.aush.repl

import ru.spbau.mit.aush.ast.ASTNode

sealed class ReplException(
        message: String? = null,
        cause: Throwable? = null) : Exception(message, cause)

object UnexpectedEOI : ReplException(
        "unexpected end of input"
)

class FailedCommandEvaluation(
        command: ASTNode,
        cause: Throwable
) : ReplException(
        "command \"$command\" evaluation failed because ${cause.message}",
        cause
)