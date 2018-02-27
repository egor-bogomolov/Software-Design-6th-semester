package ru.spbau.mit.aush.ast

import ru.spbau.mit.aush.Command
import ru.spbau.mit.aush.ExitCommand
import ru.spbau.mit.aush.evaluation.*
import ru.spbau.mit.aush.lexer.Word
import ru.spbau.mit.aush.lexer.WordPart
import java.io.PipedInputStream
import java.io.PipedOutputStream

private fun Word.interpolate(
        environmentVariables: EnvironmentVariables
): String =
        parts.joinToString {
            when (it.type) {
                WordPart.Type.PLAIN_QUOTED, WordPart.Type.UNQUOTED ->
                    "\$\\w".toRegex().replace(it.string) {
                        match -> environmentVariables[match.value.substring(1)]
                    }
                WordPart.Type.RAW_QUOTED -> it.string
            }
        }

abstract class ASTNode {
    abstract fun evaluate(
            environment: Environment
    ): EvaluationResult
}

class CommandNode(
        private val commandName: Word,
        private val args: List<Word>
) : ASTNode() {
    override fun evaluate(environment: Environment): EvaluationResult {
        return try {
            val command =
                    Command.getCommand(
                            commandName.interpolate(environment.variables)
                    )
            if (command == ExitCommand) {
                SuccessfullyExited
            } else {
                command.evaluate(
                        args.map { it.interpolate(environment.variables) },
                        environment
                )
                EvaluationSuccess(EnvironmentVariables.emptyVariables)
            }
        } catch (throwable: Throwable) {
            EvaluationFailure(throwable)
        }
    }
}

class PipeNode(
        private val left: ASTNode,
        private val right: ASTNode
) : ASTNode() {
    override fun evaluate(environment: Environment): EvaluationResult {
        val pipeIn = PipedInputStream()
        val pipeOut = PipedOutputStream()
        pipeIn.connect(pipeOut)
        val resultLeft = left.evaluate(environment.copy(
                io = environment.io.copy(output = pipeOut)
        ))
        when (resultLeft) {
            is EvaluationFailure -> return EvaluationFailure(
                    SubCommandEvaluationFailed(
                            left,
                            resultLeft.failureCause
                    )
            )
            SuccessfullyExited -> return SuccessfullyExited
        }
        val resultRight = right.evaluate(environment.copy(
                io = environment.io.copy(input = pipeIn)
        ))
        return when(resultRight) {
            is EvaluationSuccess ->
                EvaluationSuccess(EnvironmentVariables.emptyVariables)
            is EvaluationFailure -> EvaluationFailure(
                    SubCommandEvaluationFailed(
                            right,
                            resultRight.failureCause
                    )
            )
            SuccessfullyExited -> SuccessfullyExited
        }
    }
}

class DefineVariableNode(
        private val name: String,
        private val value: Word,
        private val followingNode: ASTNode
) : ASTNode() {
    constructor(nameValuePair: Pair<String, Word>, followingNode: ASTNode) :
            this(nameValuePair.first, nameValuePair.second, followingNode)

    override fun evaluate(environment: Environment) =
            followingNode.evaluate(
                    environment.copy(
                            variables = environment.variables
                                    + (name to value.interpolate(environment.variables))
                    )
            )
}

object EmptyNode : ASTNode() {
    override fun evaluate(environment: Environment): EvaluationSuccess =
            EvaluationSuccess(environment.variables)
}