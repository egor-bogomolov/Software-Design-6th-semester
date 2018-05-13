package ru.spbau.mit.roguelike.runner

import ru.spbau.mit.roguelike.creatures.Creature
import ru.spbau.mit.roguelike.creatures.Monster
import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.map.PassableCell
import ru.spbau.mit.roguelike.map.Position
import java.util.*

/**
 * Interface for creature generation
 */
interface CreatureGenerator {
    /** Generates creature given a map and game settings
     * @param settings to get difficulty from
     * @param gameMap to generate creatures onto
     * @return generated creatures
     */
    fun generateCreatures(
            settings: GameSettings,
            gameMap: GameMap
    ): Map<Position,Set<Creature>>
}

/**
 * Generates no creatures
 */
object NoCreatureGenerator: CreatureGenerator {
    override fun generateCreatures(
            settings: GameSettings,
            gameMap: GameMap
    ): Map<Position,Set<Creature>> =
            emptyMap()
}

/**
 * Generates n goblins with 10 base health and 1 base damage
 */
class NGoblinsGenerator(val n: Int): CreatureGenerator {
    private val random = Random()

    private fun randomCreature(difficulty: GameSettings.Difficulty): Creature { // TODO("resource getter for predefined monsters")
        val modifier = when (difficulty) {
            GameSettings.Difficulty.EASY   -> Monster.Modifier.ORDINARY
            GameSettings.Difficulty.NORMAL -> if (random.nextBoolean()) {
                Monster.Modifier.RANDOM
            } else {
                Monster.Modifier.ORDINARY
            }
            GameSettings.Difficulty.HARD   -> Monster.Modifier.RANDOM
        }

        return Monster(
                "goblin",
                10f,
                1f,
                modifier
        )
    }

    override fun generateCreatures(settings: GameSettings, gameMap: GameMap): Map<Position,Set<Creature>> {
        val passableCells = List(
                settings.mapDimensions.first *
                        settings.mapDimensions.second
        ) { cellId ->
            val x = cellId % settings.mapDimensions.first
            val y = cellId / settings.mapDimensions.first
            x to y
        }.filter { gameMap[it] is PassableCell }

        fun randomCell(): Position =
                passableCells[random.nextInt(passableCells.size)]

        val creatures: MutableMap<Position,MutableSet<Creature>> = mutableMapOf()

        for (i in 1..n) {
            creatures.getOrPut(
                    randomCell()
            ) { mutableSetOf() } += randomCreature(settings.difficulty)
        }

        return creatures
    }

}