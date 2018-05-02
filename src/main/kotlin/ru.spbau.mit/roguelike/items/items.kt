package ru.spbau.mit.roguelike.items

abstract class Item(
        val name: String,
        val description: String
) {
    abstract fun detailedInfo(): String
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