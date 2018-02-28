package ru.spbau.mit.aush.lexer

/**
 * Represents the result of lexing process
 */
sealed class LexResult

/**
 * Represents successful lexing and contains list of commands
 */
data class LexSuccess(val commands: List<Command>) : LexResult()

/**
 * Represents failed lexing and contains an instance of corresponding
 * {@link ru.spbau.mit.aush.lexer.LexerException}
 */
data class LexFailure(val exception: LexerException) : LexResult()