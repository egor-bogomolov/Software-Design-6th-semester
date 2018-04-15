package ru.spbau.mit.roguelike.runner

import mu.KLogging
import ru.spbau.mit.roguelike.creatures.*
import ru.spbau.mit.roguelike.hero.Hero
import ru.spbau.mit.roguelike.map.CellWithCreature
import ru.spbau.mit.roguelike.map.FoundItems
import ru.spbau.mit.roguelike.map.GameFinish
import ru.spbau.mit.roguelike.map.GameMap

class GameRunner(
        val settings: GameSettings,
        val hero: Hero,
        mapGenerator: GameMapGenerator,
        creatureGenerator: CreatureGenerator
) {
    val gameMap = creatureGenerator.generateCreatures(
            settings,
            mapGenerator.generateMap(settings)
    )

    private val creatures: MutableMap<Pair<Int,Int>,Creature> =
            emptyMap<Pair<Int,Int>,Creature>().toMutableMap()

    init {
        gameMap.enterWorld(hero)

        for (y in 1..settings.mapDimensions.second) {
            for (x in 1..settings.mapDimensions.first) {
                val cell = gameMap[x, y]
                if (cell is CellWithCreature) {
                    creatures[Pair(x, y)] = cell.creature
                }
            }
        }
    }

    var gameFinished: Boolean = false
        private set

    private fun visibleMap(position: Pair<Int,Int>): GameMap =
            gameMap // TODO("think about field of view limitation")

    fun nextTurn() {
        creatures.map { (position, creature) ->
            val action = creature.askAction(visibleMap(position))
            when (action) {
                is Move -> return@map Pair(
                        gameMap.move(position, action.direction),
                        creature
                )
                PassTurn -> {}
                is Attack -> {
                    val result = gameMap.attack(position, action.direction)

                    if (result === Died) {
                        val attackedPosition = Pair(
                                position.first + action.direction.dx,
                                position.second + action.direction.dy
                        )

                        if (creatures[attackedPosition] is Hero) {
                            gameFinished = true
                        }

                        creatures.remove(attackedPosition)
                    }
                }
                is Interact -> {
                    val result = gameMap.interact(position, action.direction)

                    when {
                        result === GameFinish    -> {
                            logger.info { "$creature successfully exited the world" }
                            gameFinished = true
                        }
                        result is FoundItems &&
                                creature is Hero -> {
                            logger.info { "$creature found items ${result.items}" }
                            creature.backpack.addAll(result.items)
                        }
                    }
                }
            }

            return@map Pair(position, creature)
        }
    }

    companion object: KLogging()
}