package ru.spbau.mit.aush.lexer

object Lexer {
    private const val PLAIN_QUOTE = '\"'
    private const val RAW_QUOTE = '\''
    private const val PIPE = '|'
    private const val WS = " \n"

    private fun lex(text: String): List<Command> {
        val commands: MutableList<Command> = mutableListOf()
        val currentCommand: MutableList<Word> = mutableListOf()
        val currentWord: MutableList<WordPart> = mutableListOf()
        var currentWordPart = ""

        fun nextWordPart(type: WordPart.Type = WordPart.Type.UNQUOTED) {
            if (currentWordPart.isNotEmpty()) {
                currentWord += WordPart(currentWordPart, type)
                currentWordPart = ""
            }
        }

        fun nextWord() {
            nextWordPart()
            if (currentWord.isNotEmpty()) {
                currentCommand += Word(currentWord.toList())
                currentWord.clear()
            }
        }

        fun nextCommand() {
            nextWord()
            if (currentCommand.isNotEmpty()) {
                commands += Command(currentCommand.toList())
                currentCommand.clear()
            }
        }

        var position = 0
        while (position < text.length) {
            when (text[position]) {
                PIPE -> nextCommand()
                in WS -> nextWord()
                RAW_QUOTE -> {
                    nextWordPart()
                    val closingQuoteIndex = text.indexOf(RAW_QUOTE, position + 1)
                    if (closingQuoteIndex == -1) {
                        throw UnclosedRawQuote
                    }
                    currentWordPart = text.substring(position + 1, closingQuoteIndex)
                    nextWordPart(WordPart.Type.RAW_QUOTED)
                    position = closingQuoteIndex
                }
                PLAIN_QUOTE -> {
                    nextWordPart()
                    val closingQuoteIndex = text.indexOf(PLAIN_QUOTE, position + 1)
                    if (closingQuoteIndex == -1) {
                        throw UnclosedPlainQuote
                    }
                    currentWordPart = text.substring(position + 1, closingQuoteIndex)
                    nextWordPart(WordPart.Type.PLAIN_QUOTED)
                    position = closingQuoteIndex
                }
                else -> currentWordPart += text[position]
            }
            position++
        }
        if (commands.isNotEmpty()
                && currentCommand.isEmpty()
                && currentWord.isEmpty()
                && currentWordPart.isEmpty()) {
            throw NoCommandAfterPipe
        }
        nextCommand()

        return commands
    }

    fun tryLex(text: String): LexResult =
            try {
                LexSuccess(lex(text))
            } catch (lexerException: LexerException) {
                LexFailure(lexerException)
            }
}