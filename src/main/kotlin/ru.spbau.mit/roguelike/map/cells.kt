package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.items.Item

sealed class TerrainCell {
    abstract fun interact(): InteractionResult
}

sealed class PassableCell(
        lyingItems: Set<Item>
): TerrainCell() {
    val lyingItems = lyingItems.toMutableSet()
}

sealed class ImpassableCell: TerrainCell()

object WorldEntrance: PassableCell(emptySet()) {
    override fun interact() = NoInteraction
}

object WorldExit: ImpassableCell() {
    override fun interact() = GameFinish
}

object WallCell: ImpassableCell() {
    override fun interact() = NoInteraction
}

class FloorCell(lyingItems: Set<Item>): PassableCell(lyingItems) {
    override fun interact() = NoInteraction
}

object OpenedDoor: PassableCell(emptySet()) {
    override fun interact() = ChangesState(ClosedDoor)
}

object ClosedDoor: ImpassableCell() {
    override fun interact() = ChangesState(OpenedDoor)
}

class OpenedChest(contents: List<Item>): ImpassableCell() {
    private val contents = contents.toMutableList()

    override fun interact() =
            CanExchangeItems(contents)
}

object OutsideGameBordersCell: ImpassableCell() {
    override fun interact() = NoInteraction
}