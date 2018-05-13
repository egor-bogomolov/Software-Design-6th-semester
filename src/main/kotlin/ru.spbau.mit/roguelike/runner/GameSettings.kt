package ru.spbau.mit.roguelike.runner

/**
 * Represents game settings
 */
class GameSettings(
    val mapDimensions: Pair<Int,Int>,
    val difficulty: Difficulty
) {
    /**
     * Represents game difficulty
     */
    enum class Difficulty {
        EASY,   // all monsters are ordinary
        NORMAL, // half of the monsters are ordinary
        HARD    // all monsters are modified
    }
}