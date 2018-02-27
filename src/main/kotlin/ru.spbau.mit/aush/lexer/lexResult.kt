package ru.spbau.mit.aush.lexer

sealed class LexResult

data class LexSuccess(val commands: List<Command>) : LexResult()

data class LexFailure(val exception: LexerException) : LexResult()