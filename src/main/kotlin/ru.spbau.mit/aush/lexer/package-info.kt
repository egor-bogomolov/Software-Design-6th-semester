package ru.spbau.mit.aush.lexer

/**
 * Contains various classes related to splitting input text to lexems
 * and some lowest-level parsing:
 * - input is split into commands
 * - commands into words
 * - words into word parts of different kinds (raw string, plain string, unquoted)
 *
 * Contents:
 * - data classes representing lexed input
 * - exceptions which can occur during lexing
 * - lexer itself
 * - lexing results used in error handling outside lexed
*/