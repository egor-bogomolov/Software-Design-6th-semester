package ru.spbau.mit.aush.repl

sealed class ReplException(
        message: String? = null,
        cause: Throwable? = null) : Exception(message, cause)

class FailedCommandEvaluation(
        command: String,
        cause: Throwable
) : ReplException(
        """
            |command "$command" evaluation failed because
            |${cause.message ?: cause}
        """.trimMargin(),
        cause
)