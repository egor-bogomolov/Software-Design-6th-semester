package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.creatures.Creature
import ru.spbau.mit.roguelike.runner.GameRunner
import kotlin.coroutines.experimental.Continuation

fun setupAttackTargetDialog(
        screen: Screen,
        gameRunner: GameRunner,
        attackedCell: Pair<Int, Int>,
        chosenTargetForwarder: Continuation<Creature>
): Screen {
    TODO("not implemented")
}