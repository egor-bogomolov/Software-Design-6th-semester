package ru.spbau.mit.roguelike.ui

import ru.spbau.mit.roguelike.hero.Hero
import ru.spbau.mit.roguelike.runner.CreatureGenerator
import ru.spbau.mit.roguelike.runner.GameMapGenerator
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.runner.GameSettings

abstract class GameUI(
        protected val mapGenerator: GameMapGenerator,
        protected val creatureGenerator: CreatureGenerator
) {
    internal lateinit var gameSettings: GameSettings
    internal lateinit var hero: Hero

    internal abstract fun setupGame()

    internal abstract fun setupHero()

    internal abstract fun runGame(gameRunner: GameRunner)

    internal abstract fun showResults(gameRunner: GameRunner)

    fun main(args: Array<String>) = setupGame()
}