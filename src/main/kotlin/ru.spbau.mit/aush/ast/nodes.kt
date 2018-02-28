package ru.spbau.mit.aush.ast

import ru.spbau.mit.aush.Command
import ru.spbau.mit.aush.Environment
import ru.spbau.mit.aush.EnvironmentVariables
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

/**
 * Represents node in abstract syntax tree of a command
 */
abstract class ASTNode {

    /**
     * Evaluates node in given environments
     *
     * @param environment environment to evaluate in
     * @return instance of {@link ru.spbau.mit.aush.evaluation.EvaluationResult} corresponding to result of evaluation
     */
    abstract fun evaluate(
            environment: Environment
    ): EvaluationResult
}

/**
 * Represents shell command with arguments
 */
data class CommandNode(
        private val commandName: Word,
        private val args: List<Word>
) : ASTNode() {

    /**
     * {@inheritDoc}
     */
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
            EvaluationFailure(
                    (listOf(commandName) + args)
                            .joinToString(" ") {
                                it.interpolate(environment.variables)
                            },
                    throwable
            )
        }
    }
}

/**
 * Represents Bash-like pipe between two nodes
 */
data class PipeNode(
        private val left: ASTNode,
        private val right: ASTNode
) : ASTNode() {

    /**
     * {@inheritDoc}
     */
    override fun evaluate(environment: Environment): EvaluationResult {
        val pipeIn = PipedInputStream()
        val pipeOut = PipedOutputStream()
        pipeIn.connect(pipeOut)
        val resultLeft = left.evaluate(environment.copy(
                io = environment.io.copy(output = pipeOut)
        ))
        pipeOut.close()
        when (resultLeft) {
            is EvaluationFailure -> return EvaluationFailure(
                    "${resultLeft.command} |",
                    CommandEvaluationFailedException(
                            resultLeft.command,
                            resultLeft.cause
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
                    "| ${resultRight.command}",
                    CommandEvaluationFailedException(
                            resultRight.command,
                            resultRight.cause
                    )
            )
            SuccessfullyExited -> SuccessfullyExited
        }
    }
}

/**
 * Represents environment variable definition
 */
data class DefineVariableNode(
        private val name: String,
        private val value: Word,
        private val followingNode: ASTNode
) : ASTNode() {
    /**
     * Convenience constructor which takes pair of (name, value)
     * rather than using them as different arguments
     */
    constructor(nameValuePair: Pair<String, Word>, followingNode: ASTNode) :
            this(nameValuePair.first, nameValuePair.second, followingNode)

    /**
     * {@inheritDoc}
     */
    override fun evaluate(environment: Environment) =
            followingNode.evaluate(
                    environment.copy(
                            variables = environment.variables
                                    + (name to value.interpolate(environment.variables))
                    )
            )
}

/**
 * Represents a no-op
 */
object EmptyNode : ASTNode() {
    override fun evaluate(environment: Environment): EvaluationSuccess =
            EvaluationSuccess(environment.variables)
}