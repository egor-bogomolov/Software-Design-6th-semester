package ru.spbau.mit.roguelike.creatures

import ru.spbau.mit.roguelike.RandomEnumGetter

/**
 * Represents abstract creature action
 */
sealed class CreatureAction

/**
 * Represents doing nothing on current turn
 */
object PassTurn: CreatureAction()

/**
 * Represents action direction
 */
enum class Direction(val dx: Int, val dy: Int) {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0),
    CURRENT(0, 0);

    companion object: RandomEnumGetter<Direction>(
            Direction::class.java
    )
}

/**
 * Represents action with a single direction
 */
sealed class DirectedAction(val direction: Direction): CreatureAction()

/**
 * Represents a move in a given direction
 */
class Move(direction: Direction): DirectedAction(direction)

/**
 * Represents an attack of a given creature in a given direction
 */
class Attack(
        direction: Direction,
        val target: Creature
): DirectedAction(direction)

/**
 * Represents interaction with a map cell
 */
class Interact(direction: Direction): DirectedAction(direction)