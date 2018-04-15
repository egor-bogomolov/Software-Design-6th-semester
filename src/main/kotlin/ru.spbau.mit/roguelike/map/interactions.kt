package ru.spbau.mit.roguelike.map

import ru.spbau.mit.roguelike.items.Item

sealed class InteractionResult

object NoInteraction: InteractionResult()

object GameFinish: InteractionResult()

class ChangesState(val newState: TerrainCell): InteractionResult()

class FoundItems(val items: List<Item>): InteractionResult()