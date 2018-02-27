package ru.spbau.mit.aush

import ru.spbau.mit.aush.lexer.Lexer
import ru.spbau.mit.aush.parser.CommandListParser
import ru.spbau.mit.aush.repl.ReplState
import java.util.*

private fun outputString(string: String) =
        ReplState.output.writer().run {
            append(string)
            flush()
        }

private fun requestInput() {
    outputString("> ")
}

fun main(args: Array<String>) {
    outputString("Welcome to AUsh (Academic University SHell)\n")
    while (true) {
        var input = ""
        var lexResult = Lexer.tryLex(input)
        while (lexResult is Lexer.LexFailure) {
            requestInput()
            input += (if (input.isEmpty()) "" else "\n")
            input += Scanner(ReplState.input).nextLine()
            lexResult = Lexer.tryLex(input)
        }
        val ast = CommandListParser.parse(
                (lexResult as Lexer.LexSuccess).commands
        )
        ast.evaluate(ReplState.environment)
    }
}