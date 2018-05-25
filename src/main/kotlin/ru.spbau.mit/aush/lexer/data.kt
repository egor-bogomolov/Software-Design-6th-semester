package ru.spbau.mit.aush.lexer

/**
 * Command consisting of Words
 */
data class LCommand(val words: List<Word>)


/**
 * Word consisting of WordParts
 */
data class Word(val parts: List<WordPart>)

/**
 * Part of a word. Can be:
 * - text without delimiters
 * - text in double quotes (`plain` string)
 * - text in single quotes (`raw` string)
 *
 * Single-quoted text is not interpolated before evaluation
 * while other two are
 */
data class WordPart(val string: String, val type: Type) {
    enum class Type {
        PLAIN_QUOTED,
        RAW_QUOTED,
        UNQUOTED
    }
}