package ru.spbau.mit.aush.lexer

/**
 * Represents error in lexing process
 */
sealed class LexerException(message: String) : Exception(message)

/**
 * Represents internal lexer cause
 */
class LexerInternalException(message: String) : LexerException(message)

/**
 * Represents no input before pipe (`|`)
 */
object NoCommandBeforePipeException : LexerException("missing command before pipe")

/**
 * Represents no input after pipe (`|`)
 */
object NoCommandAfterPipeException : LexerException("missing command after pipe")

/**
 * Represents missing pair to a quote of some kind
 */
class UnclosedQuoteException(quote: Char) : LexerException("missing pair to a '$quote'-quote")