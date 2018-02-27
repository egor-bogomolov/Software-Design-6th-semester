package ru.spbau.mit.aush.lexer

sealed class LexerException(message: String) : Exception(message)

object NoCommandAfterPipe : LexerException("missing command after pipe")

abstract class UnclosedQuote(message: String) : LexerException(message)

object UnclosedRawQuote : UnclosedQuote("pair raw quote not found")

object UnclosedPlainQuote : UnclosedQuote("pair plain quote not found")