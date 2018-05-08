package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.items.Item

sealed class TerrainCell {
    abstract fun interact(): InteractionResult

    abstract val char: Char
}

sealed class PassableCell(
        lyingItems: Set<Item>
): TerrainCell() {
    val lyingItems = lyingItems.toMutableSet()
}

sealed class ImpassableCell: TerrainCell()

object WorldEntrance: PassableCell(emptySet()) {
    override fun interact() = NoInteraction

    override val char: Char = 60.toChar()
}

object WorldExit: ImpassableCell() {
    override fun interact() = GameFinish

    override val char: Char = 62.toChar()
}

object WallCell: ImpassableCell() {
    override fun interact() = NoInteraction

    override val char: Char = 176.toChar()
}

class FloorCell(lyingItems: Set<Item>): PassableCell(lyingItems) {
    override fun interact() = NoInteraction

    override val char: Char = 226.toChar()
}

object OpenedDoor: PassableCell(emptySet()) {
    override fun interact() = ChangesState(ClosedDoor)

    override val char: Char = 'O'
}

object ClosedDoor: ImpassableCell() {
    override fun interact() = ChangesState(OpenedDoor)

    override val char: Char = 'C'
}

class Chest(contents: List<Item>): ImpassableCell() {
    private val contents = contents.toMutableList()

    override fun interact() =
            CanExchangeItems(contents)

    override val char: Char = 199.toChar()
}

object UnseenCell: ImpassableCell() {
    override fun interact() = NoInteraction

    override val char: Char = '?'

}

object OutsideGameBordersCell: ImpassableCell() {
    override fun interact() = NoInteraction

    override val char: Char = 32.toChar()
}