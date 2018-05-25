package ru.spbau.mit.aush.evaluation

import java.io.File

/**
 * Represents exceptions occurred in evaluation process
 */
sealed class EvaluationException(
        message: String? = null,
        cause: Throwable? = null) : Exception(message, cause)

/**
 * Represents a cause in evaluation of a specific command
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

class TooManyArgumentsException(expected: Int, got: Int): Exception("Too many arguments, expected: $expected, got: $got")

class FileNotExistException(file: File): Exception("File $file doesn't exist")

class NotDirectoryException(file: File): Exception("File $file isn't a directory")