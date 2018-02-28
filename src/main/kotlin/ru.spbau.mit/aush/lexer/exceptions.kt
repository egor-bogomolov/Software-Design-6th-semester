package ru.spbau.mit.aush.lexer

/**
 * Represents error in lexing process
 */
sealed class LexerException(message: String) : Exception(message)

/**
 * Represents internal lexer exception
 */
class LexerInternalException(message: String) : LexerException(message)

/**
 * Represents no input after pipe (`|`)
 */
object NoCommandAfterPipe : LexerException("missing command after pipe")

/**
 * Represents missing pair to a quote of some kind
 */
class UnclosedQuote(quote: Char) : LexerException("missing pair to a '$quote'-quote")