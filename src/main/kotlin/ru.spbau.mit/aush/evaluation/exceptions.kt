package ru.spbau.mit.aush.evaluation

sealed class EvaluationException(
        message: String? = null,
        cause: Throwable? = null) : Exception(message, cause)

class SubCommandEvaluationFailed(
        subCommand: String,
        failureCause: Throwable
) : EvaluationException(
        """
            |sub-command "$subCommand" evaluation failed because
            |${failureCause.message ?: failureCause}
        """.trimMargin(),
        failureCause
)