package ru.spbau.mit.roguelike

import ru.spbau.mit.roguelike.ui.GameUI
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI

val gameModes = mapOf<String,Pair<String,GameUI>>(
        "cli" to ("CLI" to CLIGameUI)
)

val usageMessage: String by lazy {
    StringBuilder()
            .appendln("Usage:")
            .appendln(
                    gameModes.toList().joinToString("\n") {
                        "${it.first} for ${it.second.first} game mode"
                    }
            )
            .toString()
}

fun printUsage(additionalMessage: String = "") {
    println("""
        $additionalMessage
        $usageMessage
    """.trimIndent())
}

fun main(args: Array<String>) {
    when {
        args.isEmpty() -> {
            printUsage("Game mode expected")
        }
        args.size > 1  -> {
            printUsage("Too many arguments")
        }
        else           -> {
                gameModes[args[0]]
                        ?.second
                        ?.main(args.drop(1).toTypedArray())
                        ?: printUsage("Unknown game mode \"${args[0]}\"")
        }
    }
}