package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.creatures.AttackResult
import ru.spbau.mit.roguelike.creatures.Died
import ru.spbau.mit.roguelike.creatures.Direction
import ru.spbau.mit.roguelike.creatures.NoTarget
import ru.spbau.mit.roguelike.exceptions.GameMapHasNoEntranceException
import ru.spbau.mit.roguelike.exceptions.OutsideGameBoundsExceptionGame
import ru.spbau.mit.roguelike.hero.Hero

class GameMap(cells: Map<Pair<Int,Int>,TerrainCell>) {
    private val cells = cells.toMutableMap()

    val entrance: Pair<Int,Int>

    init {
        val entrances = cells.entries.filter { it.value == WorldEntrance }
        if (entrances.isEmpty()) {
            throw GameMapHasNoEntranceException
        }
        entrance = entrances.first().key
    }

    fun enterWorld(hero: Hero): Boolean {
        val entranceCell = cells[entrance]

        return if (entranceCell is WorldEntrance) {
            cells[entrance] = CellWithCreature(hero, entranceCell)
            true
        } else {
            false
        }
    }

    operator fun get(position: Pair<Int,Int>): TerrainCell =
            cells[position] ?: throw OutsideGameBoundsExceptionGame

    operator fun get(x: Int, y: Int): TerrainCell = this[Pair(x, y)]

    fun move(
            oldPosition: Pair<Int,Int>,
            direction: Direction
    ): Pair<Int,Int> {
        val oldCell = cells[oldPosition]
                as? CellWithCreature ?: return oldPosition

        val newPosition = Pair(
                oldPosition.first + direction.dx,
                oldPosition.second + direction.dy
        )

        val newCell = cells[newPosition]
                as? PassableCell ?: return oldPosition

        if (cells[newPosition] !is PassableCell) {
            return oldPosition
        }

        cells[newPosition] = CellWithCreature(
                oldCell.creature,
                newCell
        )

        cells[oldPosition] = oldCell.underlyingCell

        return newPosition
    }

    fun attack(
            position: Pair<Int,Int>,
            direction: Direction
    ): AttackResult {
        val cell = this[position] as? CellWithCreature ?: return NoTarget

        val attackedPosition = Pair(
                position.first + direction.dx,
                position.second + direction.dy
        )

        val attackedCell = this[attackedPosition]
                as? CellWithCreature ?: return NoTarget

        val attackDamage = cell.creature.damage

        val attackResult = attackedCell
                .creature
                .takeDamage(attackDamage)

        if (attackResult === Died) {
            cells[attackedPosition] = attackedCell.underlyingCell
        }

        return attackResult
    }

    fun interact(
            position: Pair<Int,Int>,
            direction: Direction
    ): InteractionResult {
        val cell = this[position]

        if (cell !is CellWithCreature ||
                cell.creature !is Hero) {
            return NoInteraction
        }

        val interactedCell = Pair(
                position.first + direction.dx,
                position.second + direction.dy
        )

        val result = this[interactedCell].interact()

        if (result is ChangesState) {
            cells[interactedCell] = result.newState
        }

        return result
    }
}