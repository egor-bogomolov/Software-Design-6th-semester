package ru.spbau.mit.aush.lexer

data class Command(val words: List<Word>)

data class Word(val parts: List<WordPart>)

data class WordPart(val string: String, val type: Type) {
    enum class Type {
        PLAIN_QUOTED,
        RAW_QUOTED,
        UNQUOTED
    }
}