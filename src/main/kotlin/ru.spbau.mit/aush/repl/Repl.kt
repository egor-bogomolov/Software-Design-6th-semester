package ru.spbau.mit.aush.repl

import ru.spbau.mit.aush.ast.ASTNode
import ru.spbau.mit.aush.ast.Environment
import ru.spbau.mit.aush.ast.EnvironmentIO
import ru.spbau.mit.aush.ast.EnvironmentVariables
import ru.spbau.mit.aush.evaluation.EvaluationFailure
import ru.spbau.mit.aush.evaluation.EvaluationSuccess
import ru.spbau.mit.aush.evaluation.SuccessfullyExited
import ru.spbau.mit.aush.lexer.*
import ru.spbau.mit.aush.parser.CommandListParser
import java.io.PrintStream

class Repl(
        private val environmentIO: EnvironmentIO
) {
    private val input
        get()  = environmentIO.input.bufferedReader()

    private val output
        get() = PrintStream(environmentIO.output)

    private val error
        get() = PrintStream(environmentIO.error)

    var environmentVariables = EnvironmentVariables.emptyVariables

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

    fun run() {
        greet()
        runInner()
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
                    throw FailedCommandEvaluation(
                            result.command,
                            result.failureCause
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
}