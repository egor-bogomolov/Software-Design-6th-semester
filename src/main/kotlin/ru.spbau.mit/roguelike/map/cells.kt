package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.creatures.Creature
import ru.spbau.mit.roguelike.items.Item

sealed class TerrainCell {
    abstract fun interact(): InteractionResult
}

sealed class PassableCell: TerrainCell()

sealed class ImpassableCell: TerrainCell()

object WorldEntrance: PassableCell() {
    override fun interact() = NoInteraction
}

object WorldExit: PassableCell() {
    override fun interact() = GameFinish
}

object WallCell: ImpassableCell() {
    override fun interact() = NoInteraction
}

object FloorCell: PassableCell() {
    override fun interact() = NoInteraction
}

object OpenedDoor: PassableCell() {
    override fun interact() = ChangesState(ClosedDoor)
}

object ClosedDoor: ImpassableCell() {
    override fun interact() = ChangesState(OpenedDoor)
}

class OpenedChest(private var items: List<Item>): ImpassableCell() {
    override fun interact() =
            if (items.isNotEmpty()) {
                items = emptyList()
                FoundItems(items)
            } else {
                NoInteraction
            }
}

class CellWithCreature(
        val creature: Creature,
        val underlyingCell: PassableCell
): ImpassableCell() {
    override fun interact() = NoInteraction
}