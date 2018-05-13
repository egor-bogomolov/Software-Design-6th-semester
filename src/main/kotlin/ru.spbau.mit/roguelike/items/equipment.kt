package ru.spbau.mit.roguelike.items

import ru.spbau.mit.roguelike.creatures.hero.BasicStats
import ru.spbau.mit.roguelike.formatEnumValue

/**
 * Represents hero equipment which uses a specific slot and changes basic hero stats
 */
class Equipment(
        name: String,
        description: String,
        val slot: Slot,
        val stats: BasicStats
): Item(name, description) {
    /**
     * Represents possible equipment slots
     */
    enum class Slot {
        HELMET,
        ARMOR,
        GLOVES,
        SWORD,
        PANTS,
        BOOTS
    }

    override fun detailedInfo(): String {
        val itemStats = BasicStats.Type
                .values()
                .joinToString("\n") {
                    type -> "${formatEnumValue(type.name)}: ${stats[type]}"
                }
        return """$name
                |Slot: ${formatEnumValue(slot.name)}
                |Stats:
                |$itemStats
                |$description
            """.trimMargin()
    }
}