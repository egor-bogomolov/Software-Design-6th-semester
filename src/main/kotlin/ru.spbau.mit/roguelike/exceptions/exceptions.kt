package ru.spbau.mit.roguelike.exceptions

/**
 * Abstract game exception
 */
sealed class GameException(
        reason: String = "",
        cause: Throwable? = null
): Exception(reason, cause)

/**
 * Exception related to game map
 */
abstract class GameMapException(
        reason: String = "",
        cause: Throwable? = null
): GameException(reason, cause)

/**
 * Raised when map has no entrance
 */
object GameMapHasNoEntranceException: GameMapException("world has no entrance")

/**
 * Raised when map has more than one entrance
 */
object GameMapMultipleEntrancesException: GameMapException("world has too many entrances")