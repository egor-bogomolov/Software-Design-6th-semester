package ru.spbau.mit.roguelike.ui

import kotlinx.coroutines.experimental.runBlocking
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.runner.CreatureGenerator
import ru.spbau.mit.roguelike.runner.GameMapGenerator
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.runner.GameSettings
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Abstract game UI which handles current game state (settings and runner),
 * provided map and creature generators
 */
abstract class GameUI(
        private val mapGenerator: GameMapGenerator,
        private val creatureGenerator: CreatureGenerator
) {
    protected lateinit var gameSettings: GameSettings
    protected lateinit var gameRunner: GameRunner

    /**
     * Sets up the game settings and forwards it to the next stage
     * @param settingsForwarder continuation used to pass constructed GameSettings to the next stage
     */
    protected abstract fun setupGame(
            settingsForwarder: Continuation<GameSettings>
    )

    /**
     * Sets up hero and forwards him to the next stage
     * @param heroForwarder continuation used to pass constructed Hero to the next stage
     */
    internal abstract fun setupHero(
            heroForwarder: Continuation<Hero>
    )

    /**
     * Runs the game using given GameRunner
     * @param gameRunner used to run game
     */
    internal abstract fun runGame(
            gameRunner: GameRunner
    )

    /**
     * Show game results to the player and asks whether he wants to play a new game
     * @param gameRunner game runner to get game info from
     * @param newGameForwarder continuation to pass player's decision about new game
     */
    internal abstract fun showResults(
            gameRunner: GameRunner,
            newGameForwarder: Continuation<Boolean>
    )

    /**
     * GameUI main function handling all stages of the game
     */
    fun main(args: Array<String>) {
        var newGame = true

        while (newGame) {
            runBlocking {
                gameSettings = suspendCoroutine {
                    setupGame(it)
                }
                val hero = suspendCoroutine<Hero> {
                    setupHero(it)
                }
                gameRunner = GameRunner(
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