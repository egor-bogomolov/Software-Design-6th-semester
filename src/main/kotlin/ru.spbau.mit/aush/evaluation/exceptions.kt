package ru.spbau.mit.aush.evaluation

import ru.spbau.mit.aush.ast.ASTNode

sealed class EvaluationException(
        message: String? = null,
        cause: Throwable? = null) : Exception(message, cause)

class SubCommandEvaluationFailed(
        subCommandNode: ASTNode,
        failureCause: Throwable
) : EvaluationException(
        "Sub-command \"$subCommandNode\" evaluation failed",
        failureCause
)