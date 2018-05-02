package ru.spbau.mit.roguelike.items

import ru.spbau.mit.roguelike.creatures.hero.BasicStats

class Equipment(
        name: String,
        description: String,
        val slot: Slot,
        val stats: BasicStats
): Item(name, description) {
    enum class Slot {
        HELMET,
        ARMOR,
        GLOVES,
        SWORD,
        PANTS,
        BOOTS
    }
}