package ru.spbau.mit.roguelike.ui

import kotlinx.coroutines.experimental.runBlocking
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.runner.CreatureGenerator
import ru.spbau.mit.roguelike.runner.GameMapGenerator
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.runner.GameSettings
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

abstract class GameUI(
        internal val mapGenerator: GameMapGenerator,
        internal val creatureGenerator: CreatureGenerator
) {
    internal abstract fun setupGame(
            settingsForwarder: Continuation<GameSettings>
    )

    internal abstract fun setupHero(
            heroForwarder: Continuation<Hero>
    )

    internal abstract fun runGame(
            gameRunner: GameRunner
    )

    internal abstract fun showResults(
            gameRunner: GameRunner,
            newGameForwarder: Continuation<Boolean>
    )

    fun main(args: Array<String>) {
        var newGame = true

        while (newGame) {
            runBlocking {
                val gameSettings = suspendCoroutine<GameSettings> {
                    setupGame(it)
                }
                val hero = suspendCoroutine<Hero> {
                    setupHero(it)
                }
                val gameRunner = GameRunner(
                        gameSettings,
                        hero,
                        mapGenerator,
                        creatureGenerator
                )
                runGame(gameRunner)
                newGame = suspendCoroutine {
                    showResults(gameRunner, it)
                }
            }
        }
    }
}