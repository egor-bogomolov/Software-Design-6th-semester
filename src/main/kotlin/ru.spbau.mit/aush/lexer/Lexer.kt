package ru.spbau.mit.aush.lexer

/**
 * Represents command language lexer
 * Initial error checking is also performed here
 * (closed quotes, command after pipe)
 */
class Lexer private constructor(private val text: String) {
    private val commands: MutableList<Command> = mutableListOf()
    private val currentCommand: MutableList<Word> = mutableListOf()
    private val currentWord: MutableList<WordPart> = mutableListOf()
    private var currentWordPart = ""

    private fun nextWordPart(type: WordPart.Type = WordPart.Type.UNQUOTED) {
        if (currentWordPart.isNotEmpty()) {
            currentWord += WordPart(currentWordPart, type)
            currentWordPart = ""
        }
    }

    private fun nextWord() {
        nextWordPart()
        if (currentWord.isNotEmpty()) {
            currentCommand += Word(currentWord.toList())
            currentWord.clear()
        }
    }

    private fun nextCommand() {
        nextWord()
        if (currentCommand.isNotEmpty()) {
            commands += Command(currentCommand.toList())
            currentCommand.clear()
        }
    }

    private fun processQuotedWordPart(quote: Char, position: Int): Int {
        val type = when (quote) {
            RAW_QUOTE -> WordPart.Type.RAW_QUOTED
            PLAIN_QUOTE -> WordPart.Type.PLAIN_QUOTED
            else -> throw LexerInternalException("got unexpected character '$quote' as a quote")
        }
        nextWordPart()

        val closingQuoteIndex = text.indexOf(quote, position + 1)
        if (closingQuoteIndex == -1) {
            throw UnclosedQuote(quote)
        }
        currentWordPart = text.substring(position + 1, closingQuoteIndex)
        nextWordPart(type)

        return closingQuoteIndex
    }

    private fun lex(): List<Command> {
        var position = 0
        while (position < text.length) {
            when (text[position]) {
                PIPE -> nextCommand()
                in WS -> nextWord()
                RAW_QUOTE ->
                    position = processQuotedWordPart(RAW_QUOTE, position)
                PLAIN_QUOTE ->
                    position = processQuotedWordPart(PLAIN_QUOTE, position)
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

    companion object {
        private const val PLAIN_QUOTE = '\"'
        private const val RAW_QUOTE = '\''
        private const val PIPE = '|'
        private const val WS = " \n"

        /**
         * Runs lexer on a given string
         *
         * @param text string to process
         * @return instance of {@link ru.spbau.mit.aush.lexer.LexResult} corresponding to the result
         */
        fun tryLex(text: String): LexResult =
                try {
                    LexSuccess(Lexer(text).lex())
                } catch (lexerException: LexerException) {
                    LexFailure(lexerException)
                }
    }
}