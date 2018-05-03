package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.component.Panel
import org.codetome.zircon.api.component.builder.TextBoxBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.input.MouseActionType
import org.codetome.zircon.api.screen.Screen
import org.codetome.zircon.internal.component.impl.DefaultButton
import org.codetome.zircon.internal.component.impl.DefaultTextBox
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.setup.MouseEventHandler
import ru.spbau.mit.roguelike.ui.cli.setup.itemInfoLayer
import kotlin.math.max
import kotlin.math.min

internal class HeroInventory(
        position: Position,
        size: Size,
        gameScreen: Screen,
        gameRunner: GameRunner,
        refreshCallback: () -> Unit
): GameScreenComponent(position, size, gameScreen, gameRunner, refreshCallback) {
    override val panel: Panel = panelBuilder
            .title("Inventory")
            .build()

    private val lines: Int = panel.getEffectiveSize().rows
    private var scroll: Int = 0
    private val maxScroll: Int
        get() = max(0, gameRunner.hero.backpack.size - lines)

    private val textBox: DefaultTextBox = TextBoxBuilder.newBuilder()
            .size(panel.getEffectiveSize())
            .build() as DefaultTextBox

    private var displayedLayer: Layer? = null

    init {
        panel.addComponent(textBox)

        textBox.disable()

        gameScreen.addComponent(panel)

        panel.onMouseMoved(MouseEventHandler {
            if (it.actionType == MouseActionType.MOUSE_EXITED) {
                removeDisplayedLayer()
            } else {
                updateDisplayedLayer(getRow(it.position))
            }
            gameScreen.display()
        })

        DefaultButton

        textBox.onMouseReleased(MouseEventHandler {
            println("Release: $it")
            when (it.button) {
                1 -> gameRunner.hero.equipItem(scroll + getRow(it.position))
                2 -> gameRunner.hero.dropItem(scroll + getRow(it.position)) // TODO("implement drop to map cell")
                4 -> scroll = max(0, scroll - 1)
                5 -> scroll = min(maxScroll, scroll + 1)
            }
            refreshCallback()
        })

        refresh()
    }

    private fun getRow(position: Position): Int =
            (position - textBox.getPosition()).row

    private fun removeDisplayedLayer() {
        displayedLayer?.let { gameScreen.removeLayer(it) }
    }

    private fun updateDisplayedLayer(index: Int) {
        if (index in 0..(lines - 1)) {
            removeDisplayedLayer()
            val newLayer = itemInfoLayer(
                    Position.OFFSET_1x1
                            .withRelativeRow(index),
                    gameRunner.hero.backpack[index]
            )
            gameScreen.pushLayer(newLayer)
            displayedLayer = newLayer
            gameScreen.display()
        }
    }

    override fun refresh() {
        removeDisplayedLayer()
        displayedLayer?.let { gameScreen.pushLayer(it) }
        textBox.setText(
                gameRunner.hero.backpack
                        .drop(scroll)
                        .joinToString("\n") {
                            it.name
                        }
        )
    }
}
