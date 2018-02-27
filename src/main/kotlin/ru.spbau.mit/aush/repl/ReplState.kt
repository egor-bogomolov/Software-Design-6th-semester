package ru.spbau.mit.aush.repl

import ru.spbau.mit.aush.parser.ASTNode

object ReplState {
    var environment = Environment(
            mapOf(),
            System.`in`,
            System.out,
            System.err
    )
        private set

    val input
        get() = environment.input

    val output
        get() = environment.output

    val error
        get() = environment.error

    object SubmitVariablesNode : ASTNode() {
        override fun evaluate(environment: Environment) {
            ReplState.environment =
                    ReplState.environment.copy(
                            variables = environment.variables
                    )
        }
    }
}