package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.creatures.Direction

operator fun Position.plus(direction: Direction) =
        first + direction.dx to second + direction.dy