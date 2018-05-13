package ru.spbau.mit.roguelike.runner

import kotlinx.coroutines.experimental.runBlocking
import ru.spbau.mit.roguelike.Logger
import ru.spbau.mit.roguelike.creatures.*
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.map.*

/**
 * Represents game runner which initialises and runs the game
 */
class GameRunner(
        val settings: GameSettings,
        val hero: Hero,
        mapGenerator: GameMapGenerator,
        creatureGenerator: CreatureGenerator
) {
    /**
     * Game map
     */
    val gameMap: GameMap = mapGenerator.generateMap(settings)

    /**
     * Creature manager
     */
    val creatureManager: CreatureManager =
            CreatureManager(
                    hero,
                    creatureGenerator.generateCreatures(settings, gameMap),
                    gameMap
            )

    /**
     * Is game finished
     */
    var gameFinished: Boolean = false
        private set

    /**
     * Current turn log
     */
    var turnLog: List<String> = emptyList()
        private set

    private fun visibleMap(position: Pair<Int,Int>): GameMap =
            gameMap // TODO("think about field of view limitation")

    /**
     * Gets map part which is visible to hero
     */
    fun heroVisibleMap(): GameMap =
            visibleMap(creatureManager.heroPosition)

    /**
     * Asks next action of a creature and processes it
     * @param position of a creature
     * @param creature action of which we are going to process
     */
    private suspend fun processAction(
            position: Pair<Int, Int>,
            creature: Creature
    ) {
        val visibleMap = visibleMap(position)

        val action: CreatureAction =
                creature.askAction(
                        position,
                        visibleMap,
                        creatureManager.creatures.filterKeys {
                            visibleMap[it] !== UnseenCell
                        }
                )
        when (action) {
            is Move -> creatureManager.processMove(
                    creature,
                    position,
                    action.direction
            )
            is Attack -> creatureManager.processAttack(
                    creature,
                    position,
                    action.direction,
                    action.target
            )
            is Interact -> {
                if (creature !is Hero) {
                    return // non-Hero creatures are not supposed to interact
                }

                val result = gameMap.interact(position + action.direction)

                when {
                    result === GameFinish -> {
                        Logger.log("${creature.name} successfully exited the world")
                        gameFinished = true
                    }
                    result is CanExchangeItems -> {
                        creature.exchangeItems(result.items)
                    }
                }
            }
        }
    }

    /**
     * Processes exactly one action of each creature on the map and updates game state
     */
    fun nextTurn() {
        runBlocking {
            processAction(
                    creatureManager.heroPosition,
                    creatureManager.hero
            )
            for ((position, creatures) in creatureManager.creatures.toList()) {
                for (creature in creatures.toList()) {
                    if (creature !is Hero) {
                        processAction(position, creature)
                    }
                }
            }
            gameFinished = gameFinished || !creatureManager.heroAlive
            turnLog = Logger.getNewVisible(heroVisibleMap()).map { it.toString() }
        }
    }
}