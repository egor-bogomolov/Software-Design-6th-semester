package ru.spbau.mit.roguelike.runner

class GameSettings(
    val mapDimensions: Pair<Int,Int>,
    val difficulty: Difficulty
) {
    enum class Difficulty {
        EASY,
        NORMAL,
        HARD
    }
}