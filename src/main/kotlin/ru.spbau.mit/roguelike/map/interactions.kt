package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.items.Item

sealed class InteractionResult

object NoInteraction: InteractionResult()

object GameFinish: InteractionResult()

class ChangesState(internal val newState: TerrainCell): InteractionResult()

class CanExchangeItems(val items: MutableList<Item>): InteractionResult()