package ru.spbau.mit.aush.lexer

sealed class LexerException(message: String) : Exception(message)

object UnclosedRawQuote : LexerException("pair raw quote not found")

object UnclosedPlainQuote : LexerException("pair plain quote not found")

object NoCommandAfterPipe : LexerException("missing command after pipe")