package ru.spbau.mit.roguelike.exceptions

sealed class GameException(
        reason: String = "",
        cause: Throwable? = null
): Exception(reason, cause)

abstract class GameMapException(
        reason: String = "",
        cause: Throwable? = null
): GameException(reason, cause)

object GameMapHasNoEntranceException: GameMapException("world has no entrance")
object GameMapMultipleEntrancesException: GameMapException("world has too many entrances")