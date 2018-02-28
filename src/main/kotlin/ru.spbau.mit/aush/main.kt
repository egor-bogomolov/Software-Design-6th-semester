package ru.spbau.mit.aush

import ru.spbau.mit.aush.ast.EnvironmentIO

/**
 * Runs the REPL with System streams as its arguments
 */
fun main(args: Array<String>) {
    Repl(
            EnvironmentIO(
                    System.`in`,
                    System.out,
                    System.err
            )
    ).run()
}