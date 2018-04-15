package ru.spbau.mit.roguelike.items

abstract class Item(
        val name: String,
        val description: String
) {
    override fun toString(): String = name
}

class Junk(
        name: String,
        description: String
): Item(name, description)