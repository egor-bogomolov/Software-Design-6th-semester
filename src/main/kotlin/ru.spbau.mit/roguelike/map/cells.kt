package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.items.Item

/**
 * Represents abstract game map cell
 */
sealed class TerrainCell {
    /**
     * Interacts with a cell
     * @return result of interaction
     */
    abstract fun interact(): InteractionResult

    /**
     * UI cell representation in CP437
     */
    abstract val char: Char
}

/**
 * Represents creature passable cell (can also hold some items lying on the ground)
 */
sealed class PassableCell(
        lyingItems: List<Item>
): TerrainCell() {
    val lyingItems = lyingItems.toMutableList()
}

/**
 * Represents impassable cell
 */
sealed class ImpassableCell: TerrainCell()

/**
 * Represents world entrance (should be a single cell on a map)
 */
object WorldEntrance: PassableCell(emptyList()) {
    override fun interact() = NoInteraction

    override val char: Char = 60.toChar()
}

/**
 * Represents world exit
 */
object WorldExit: ImpassableCell() {
    /**
     * Finishes game
     */
    override fun interact() = GameFinish

    override val char: Char = 62.toChar()
}

/**
 * Represents a wall
 */
object WallCell: ImpassableCell() {
    override fun interact() = NoInteraction

    override val char: Char = 176.toChar()
}

/**
 * Represents terrain floor
 */
class FloorCell(lyingItems: List<Item>): PassableCell(lyingItems) {
    override fun interact() = NoInteraction

    override val char: Char = 226.toChar()
}

/**
 * Represents an opened door
 */
object OpenedDoor: PassableCell(emptyList()) {
    /**
     * Closes the door
     */
    override fun interact() = ChangesState(ClosedDoor)

    override val char: Char = 'O'
}

/**
 * Represents a closed door
 */
object ClosedDoor: ImpassableCell() {
    /**
     * Opens the door
     */
    override fun interact() = ChangesState(OpenedDoor)

    override val char: Char = 'C'
}

/**
 * Represents a chest
 */
class Chest(contents: List<Item>): ImpassableCell() {
    private val contents = contents.toMutableList()

    /**
     * Allows to exchange items between chest and inventory
     */
    override fun interact() =
            CanExchangeItems(contents)

    override val char: Char = 199.toChar()
}

/**
 * Represents a cell, which contents are unseen
 */
object UnseenCell: ImpassableCell() {
    override fun interact() = NoInteraction

    override val char: Char = '?'

}

/**
 * Represents a cell outside game borders
 */
object OutsideGameBordersCell: ImpassableCell() {
    override fun interact() = NoInteraction

    override val char: Char = 32.toChar()
}