package ru.spbau.mit.roguelike.items

/**
 * Represents abstract item
 */
abstract class Item(
        val name: String,
        val description: String
) {
    /**
     * Gets detailed item information
     */
    protected abstract fun detailedInfo(): String

    /**
     * Gets detailed item info but wraps it to given line width
     * @param lineWidth to trim to
     * @return wrapped text
     */
    fun detailedInfo(lineWidth: Int): String =
            detailedInfo()
                    .lines()
                    .joinToString("\n") { line ->
                        line
                                .chunked(lineWidth) { it } // TODO("add normal wrapping")
                                .joinToString("\n")
                    }
}

/**
 * Represents useless items
 */
class Junk(
        name: String,
        description: String
): Item(name, description) {
    override fun detailedInfo(): String =
            """$name
                |$description
            """.trimMargin()
}