package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.component.Panel
import org.codetome.zircon.api.component.builder.PanelBuilder
import org.codetome.zircon.api.screen.Screen
import org.codetome.zircon.internal.graphics.BoxType
import ru.spbau.mit.roguelike.runner.GameRunner

internal abstract class GameScreenComponent(
        position: Position,
        size: Size,
        val gameScreen: Screen,
        val gameRunner: GameRunner,
        val refreshCallback: () -> Unit
) {

    protected val panelBuilder = PanelBuilder.newBuilder()
            .size(size)
            .position(position)
            .wrapWithBox()
            .boxType(BoxType.TOP_BOTTOM_DOUBLE)

    abstract val panel: Panel

    abstract fun refresh()
}
