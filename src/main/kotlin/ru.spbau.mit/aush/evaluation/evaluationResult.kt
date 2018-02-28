package ru.spbau.mit.aush.evaluation

import ru.spbau.mit.aush.EnvironmentVariables

/**
 * Represents result of command evaluation
 */
sealed class EvaluationResult

/**
 * Represents succeeded evaluation and
 * contains updated environment variables
 */
class EvaluationSuccess(
        val modifiedEnvironmentVariables: EnvironmentVariables
) : EvaluationResult()

/**
 * Represents REPL exit as a result of `exit` command call
 */
object SuccessfullyExited : EvaluationResult()

/**
 * Represents evaluation failure due to an cause and
 * contains command as a string
 * (after applying environment variable interpolation)
 * and failure cause
 */
class EvaluationFailure(
        val command: String,
        val cause: Throwable
) : EvaluationResult()