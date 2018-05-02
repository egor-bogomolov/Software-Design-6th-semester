package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.exceptions.GameMapHasNoEntranceException
import ru.spbau.mit.roguelike.exceptions.GameMapMultipleEntrancesException

typealias Position=Pair<Int,Int>

class GameMap(cells: Map<Position,TerrainCell>) {
    private val cells = cells.toMutableMap()

    val entrance: Position

    init {
        val entrances = cells.entries.filter { it.value == WorldEntrance }
        when {
            entrances.isEmpty() -> throw GameMapHasNoEntranceException
            entrances.size > 1  -> throw GameMapMultipleEntrancesException
            else                -> entrance = entrances.first().key
        }
    }

    operator fun get(position: Position): TerrainCell =
            cells[position] ?: OutsideGameBordersCell

    operator fun get(x: Int, y: Int): TerrainCell = this[Pair(x, y)]

    fun interact(
            position: Position
    ): InteractionResult {
        val cell = this[position]

        val result = cell.interact()

        if (result is ChangesState) {
            cells[position] = result.newState
        }

        return result
    }
}