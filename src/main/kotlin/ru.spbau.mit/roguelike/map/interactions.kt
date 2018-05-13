package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.items.Item

/**
 * Represents abstract interaction result
 */
sealed class InteractionResult

/**
 * Represents cell without interactions
 */
object NoInteraction: InteractionResult()

/**
 * Represents game finish
 */
object GameFinish: InteractionResult()

/**
 * Represents that cell changes its state on interaction
 */
class ChangesState(internal val newState: TerrainCell): InteractionResult()

/**
 * Represents that cell has some items to exchange with
 */
class CanExchangeItems(val items: MutableList<Item>): InteractionResult()