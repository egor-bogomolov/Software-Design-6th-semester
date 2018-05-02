package ru.spbau.mit.roguelike.ui.cli.setup

import ru.spbau.mit.roguelike.items.Item
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import kotlin.coroutines.experimental.Continuation

internal fun CLIGameUI.showItemExchangeDialog(
        availableItems: MutableList<Item>,
        continuation: Continuation<Unit>
) {
    // TODO("research Zircon API and implement dialog")
    continuation.resume(Unit)
}