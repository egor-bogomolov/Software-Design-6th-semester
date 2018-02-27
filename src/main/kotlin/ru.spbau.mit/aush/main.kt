package ru.spbau.mit.aush

import ru.spbau.mit.aush.ast.EnvironmentIO
import ru.spbau.mit.aush.repl.Repl

fun main(args: Array<String>) {
    Repl(
            EnvironmentIO(
                    System.`in`,
                    System.out,
                    System.err
            )
    ).run()
}