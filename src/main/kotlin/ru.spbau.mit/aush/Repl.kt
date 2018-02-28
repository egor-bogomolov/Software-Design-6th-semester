package ru.spbau.mit.aush

import ru.spbau.mit.aush.ast.ASTNode
import ru.spbau.mit.aush.evaluation.CommandEvaluationFailedException
import ru.spbau.mit.aush.evaluation.EvaluationFailure
import ru.spbau.mit.aush.evaluation.EvaluationSuccess
import ru.spbau.mit.aush.evaluation.SuccessfullyExited
import ru.spbau.mit.aush.lexer.*
import ru.spbau.mit.aush.parser.CommandListParser
import java.io.PrintStream

/**
 * Represents the shell running with given streams
 */
class Repl(
        private val environmentIO: EnvironmentIO
) {
    private val input = environmentIO.input.bufferedReader()

    private val output = PrintStream(environmentIO.output)

    private var environmentVariables = EnvironmentVariables.emptyVariables

    private fun greet() =
            output.println("Welcome to AUsh (Academic University SHell)")

    private fun promptInput() {
        output.print("> ")
    }

    private fun readLine(): String? {
        promptInput()
        return input.readLine()
    }

    private fun readCommand(): ASTNode? {
        var commandsString = readLine() ?: return null
        var lexResult = Lexer.tryLex(commandsString)
        while (lexResult is LexFailure
                && (lexResult.exception is UnclosedQuote
                        || lexResult.exception == NoCommandAfterPipe)) {
            commandsString += "\n" + readLine()
            lexResult = Lexer.tryLex(commandsString)
        }
        return when (lexResult) {
            is LexFailure -> throw lexResult.exception
            is LexSuccess -> CommandListParser.parse(lexResult.commands)
        }
    }

    private tailrec fun runInner() {
        try {
            val command = readCommand() ?: return
            val result = command.evaluate(
                    Environment(environmentVariables, environmentIO)
            )
            when (result) {
                is EvaluationSuccess -> {
                    environmentVariables += result.modifiedEnvironmentVariables
                }
                SuccessfullyExited -> return
                is EvaluationFailure ->
                    throw CommandEvaluationFailedException(
                            result.command,
                            result.cause
                    )
            }
        } catch (throwable: Throwable) {
            output.println(
                    """
                        |Evaluation failed because
                        |${throwable.message ?: throwable}
                    """.trimMargin()
            )
        }
        runInner()
    }

    /**
     * Greets the user and runs the main shell loop
     */
    fun run() {
        greet()
        runInner()
    }
}