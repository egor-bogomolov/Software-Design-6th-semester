package ru.spbau.mit.aush.evaluation

/**
 * Represents exceptions occurred in evaluation process
 */
sealed class EvaluationException(
        message: String? = null,
        cause: Throwable? = null) : Exception(message, cause)

/**
 * Represents an exception in evaluation of a specific command
 */
class CommandEvaluationFailedException(
        command: String,
        cause: Throwable
) : EvaluationException(
        """
            |command "$command" evaluation failed because
            |${cause.message ?: cause}
        """.trimMargin(),
        cause
)