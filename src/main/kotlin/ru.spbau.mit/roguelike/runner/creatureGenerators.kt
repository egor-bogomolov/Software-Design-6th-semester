package ru.spbau.mit.roguelike.runner

import ru.spbau.mit.roguelike.map.GameMap

interface CreatureGenerator {
    fun generateCreatures(settings: GameSettings, gameMap: GameMap): GameMap
}

object NoCreatureGenerator: CreatureGenerator {
    override fun generateCreatures(settings: GameSettings, gameMap: GameMap) = gameMap
}