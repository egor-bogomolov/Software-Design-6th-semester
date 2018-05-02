package ru.spbau.mit.roguelike.runner

import ru.spbau.mit.roguelike.creatures.Creature
import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.map.Position

interface CreatureGenerator {
    fun generateCreatures(
            settings: GameSettings,
            gameMap: GameMap
    ): Map<Position,Set<Creature>>
}

object NoCreatureGenerator: CreatureGenerator {
    override fun generateCreatures(
            settings: GameSettings,
            gameMap: GameMap
    ): Map<Position,Set<Creature>> =
            emptyMap()
}