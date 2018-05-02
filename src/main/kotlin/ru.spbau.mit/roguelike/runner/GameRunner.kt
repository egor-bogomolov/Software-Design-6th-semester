package ru.spbau.mit.roguelike.runner

import kotlinx.coroutines.experimental.runBlocking
import ru.spbau.mit.roguelike.Logger
import ru.spbau.mit.roguelike.creatures.*
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.map.CanExchangeItems
import ru.spbau.mit.roguelike.map.GameFinish
import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.map.plus

class GameRunner(
        val settings: GameSettings,
        val hero: Hero,
        mapGenerator: GameMapGenerator,
        creatureGenerator: CreatureGenerator
) {
    val gameMap: GameMap = mapGenerator.generateMap(settings)

    private val creatureManager: CreatureManager =
            CreatureManager(
                    hero,
                    creatureGenerator.generateCreatures(settings, gameMap),
                    gameMap
            )

    var gameFinished: Boolean = false
        private set

    private fun visibleMap(position: Pair<Int,Int>): GameMap =
            gameMap // TODO("think about field of view limitation")

    private suspend fun processAction(
            position: Pair<Int, Int>,
            creature: Creature
    ) {
        val action: CreatureAction =
                creature.askAction(visibleMap(position))
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

    fun nextTurn() {
        runBlocking {
            processAction(
                    creatureManager.heroPosition,
                    creatureManager.hero
            )
            for ((position, creatures) in creatureManager.creatures) {
                for (creature in creatures) {
                    if (creature !is Hero) {
                        processAction(position, creature)
                    }
                }
            }
            gameFinished = gameFinished && creatureManager.heroAlive
        }
    }
}