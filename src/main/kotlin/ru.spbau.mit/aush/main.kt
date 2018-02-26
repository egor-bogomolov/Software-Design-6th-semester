package ru.spbau.mit.aush

import com.github.h0tk3y.betterParse.grammar.parseToEnd

fun main(args: Array<String>) {
    println("Welcome to AUsh (Academic University SHell)")
    while (true) {
        print("> ")
        AUshGrammar.parseToEnd(System.`in`)
    }
}