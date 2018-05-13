package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.exceptions.GameMapHasNoEntranceException
import ru.spbau.mit.roguelike.exceptions.GameMapMultipleEntrancesException

/**
 * Represents game map and processes interactions with its cells
 */
class GameMap(cells: Map<Position,TerrainCell>) {
    private val cells = cells.toMutableMap()

    /**
     * Map entrance
     */
    val entrance: Position

    init {
        val entrances = cells.entries.filter { it.value == WorldEntrance }
        when {
            entrances.isEmpty() -> throw GameMapHasNoEntranceException
            entrances.size > 1  -> throw GameMapMultipleEntrancesException
            else                -> entrance = entrances.first().key
        }
    }

    /**
     * Convenience method for getting cell at a given position
     * @param position to get cell at
     * @return cell at specified position (if exists) or OutsideGameBordersCell otherwise
     */
    operator fun get(position: Position): TerrainCell =
            cells[position] ?: OutsideGameBordersCell

    /**
     * Convenience method for getting cell at a position specified by x and y
     * @param x coordinate
     * @param y coordinate
     * @return get(Position(x, y))
     */
    operator fun get(x: Int, y: Int): TerrainCell = this[Pair(x, y)]

    /**
     * Processes interaction with a specific cell
     * @param position cell to interact with
     * @return interaction result
     */
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