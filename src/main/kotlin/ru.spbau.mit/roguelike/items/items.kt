package ru.spbau.mit.roguelike.items

abstract class Item(
        val name: String,
        val description: String
) {
    protected abstract fun detailedInfo(): String

    fun detailedInfo(lineWidth: Int): String =
            detailedInfo()
                    .lines()
                    .joinToString("\n") { line ->
                        line
                                .chunked(lineWidth) { it }
                                .joinToString("\n")
                    }
}

class Junk(
        name: String,
        description: String
): Item(name, description) {
    override fun detailedInfo(): String =
            """$name
                |$description
            """.trimMargin()
}