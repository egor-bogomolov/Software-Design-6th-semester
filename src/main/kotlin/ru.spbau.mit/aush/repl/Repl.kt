package ru.spbau.mit.aush.repl

import ru.spbau.mit.aush.ast.ASTNode
import ru.spbau.mit.aush.ast.Environment
import ru.spbau.mit.aush.ast.EnvironmentIO
import ru.spbau.mit.aush.ast.EnvironmentVariables
import ru.spbau.mit.aush.evaluation.EvaluationFailure
import ru.spbau.mit.aush.evaluation.EvaluationSuccess
import ru.spbau.mit.aush.evaluation.SuccessfullyExited
import ru.spbau.mit.aush.lexer.LexFailure
import ru.spbau.mit.aush.lexer.LexSuccess
import ru.spbau.mit.aush.lexer.Lexer
import ru.spbau.mit.aush.lexer.UnclosedQuote
import ru.spbau.mit.aush.parser.CommandListParser
import java.io.PrintStream
import java.util.*

class Repl(
        private val environmentIO: EnvironmentIO
) {
    private val input = Scanner(environmentIO.input)
    private val output = PrintStream(environmentIO.output)
    private val error = PrintStream(environmentIO.error)

    var environmentVariables = EnvironmentVariables.emptyVariables

    fun greet() =
            output.println("Welcome to AUsh (Academic University SHell)")

    private fun promptInput() =
            output.print("> ")

    private fun readLine(): String {
        promptInput()
        return input.nextLine()
    }

    private fun readCommand(): ASTNode {
        var commandsString = ""
        var lexResult = Lexer.tryLex(commandsString)
        while (lexResult is LexFailure && lexResult.exception is UnclosedQuote) {
            val inputLine = readLine()
            commandsString += (if (commandsString.isEmpty()) "" else "\n") + inputLine
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
            val command = readCommand()
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
                            command,
                            result.failureCause
                    )
            }
        } catch (throwable: Throwable) {
            error.println(
                    "Evaluation failed: ${
                        throwable.message ?: "no reason supplied"
                    }"
            )
        }
        runInner()
    }
}