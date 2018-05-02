package ru.spbau.mit.roguelike.creatures

sealed class CreatureAction

object PassTurn: CreatureAction()

enum class Direction(val dx: Int, val dy: Int) {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0),
    CURRENT(0, 0)
}

sealed class DirectedAction(val direction: Direction): CreatureAction()

class Move(direction: Direction): DirectedAction(direction)

class Attack(
        direction: Direction,
        val target: Creature
): DirectedAction(direction)

class Interact(direction: Direction): DirectedAction(direction)