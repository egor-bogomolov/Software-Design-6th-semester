package ru.spbau.mit.aush.evaluation

import ru.spbau.mit.aush.ast.EnvironmentVariables

sealed class EvaluationResult

class EvaluationSuccess(
        val modifiedEnvironmentVariables: EnvironmentVariables
) : EvaluationResult()

object SuccessfullyExited : EvaluationResult()

class EvaluationFailure(
        val failureCause: Throwable
) : EvaluationResult()