package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.creatures.Direction
import kotlin.math.abs

/**
 * Represents position on a map
 */
typealias Position=Pair<Int,Int>

/**
 * Calculates manhattan distance to other position
 * @param other position to calculate distance to
 * @return distance (abs(dx) + abs(dy))
 */
fun Position.manhattanDistance(other: Position): Int =
        abs(first - other.first) +
                abs(second - other.second)

/**
 * Calculates next position in a given direction
 * @param direction to add
 */
operator fun Position.plus(direction: Direction) =
        first + direction.dx to second + direction.dy