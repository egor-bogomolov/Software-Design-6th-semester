package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.LayerBuilder
import org.codetome.zircon.api.component.Panel
import org.codetome.zircon.api.component.builder.PanelBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.screen.Screen
import org.codetome.zircon.internal.graphics.BoxType
import ru.spbau.mit.roguelike.runner.GameRunner

/**
 * Represents abstract game screen component
 */
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

    /**
     * Zircon panel to contain information in
     */
    abstract val panel: Panel

    /**
     * Refreshes contents
     */
    abstract fun refresh()

    /**
     * Information to show if mouse moves
     */
    open fun onMouseMoved(position: Position): Layer? = null

    companion object {
        val EMPTY_LAYER: Layer = LayerBuilder.newBuilder().build()
    }
}
