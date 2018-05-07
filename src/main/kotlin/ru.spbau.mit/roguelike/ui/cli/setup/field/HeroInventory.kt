package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.component.Panel
import org.codetome.zircon.api.component.TextBox
import org.codetome.zircon.api.component.builder.TextBoxBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.input.MouseAction
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.setup.MouseEventHandler
import ru.spbau.mit.roguelike.ui.cli.setup.itemInfoLayer
import java.util.function.Consumer
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

    private val scrollHandler = Consumer<MouseAction> {
        scroll = when (it.button) {
            4    -> max(0, scroll - 1)
            5    -> min(maxScroll, scroll + 1)
            else -> return@Consumer
        }
        refresh()
        gameScreen.display()
    }

    private val itemTextBoxes: Array<TextBox> = Array(lines) {row ->
        val textBox = TextBoxBuilder.newBuilder()
                .size(Size.of(panel.getEffectiveSize().columns, 1))
                .position(Position.of(0, row))
                .build()
        textBox.disable()
        panel.addComponent(textBox)

        textBox.onMouseReleased(MouseEventHandler {
            if (it.button == 1) {
                val index = scroll + row
                if (index < gameRunner.hero.backpack.size) {
                    gameRunner.hero.equipItem(index)
                    scroll = min(scroll, maxScroll)
                    refreshCallback()
                }
            }
        }.andThen(scrollHandler))

        return@Array textBox
    }

    private val itemInfoLayers: Array<Layer> = Array(lines) { EMPTY_LAYER }

    init {
        gameScreen.addComponent(panel)
        refresh()

        panel.onMouseReleased(scrollHandler)
    }

    override fun refresh() {
        for (textBox in itemTextBoxes) {
            textBox.setText("")
        }
        for (layer in itemInfoLayers) {
            gameScreen.removeLayer(layer)
        }

        for ((row, item) in gameRunner.hero.backpack
                .drop(scroll)
                .take(lines)
                .withIndex()) {

            itemTextBoxes[row].setText(item.name)

            itemInfoLayers[row] = itemInfoLayer(
                    panel.getEffectiveSize().columns - 1,
                    Position.OFFSET_1x1
                            .withRelative(itemTextBoxes[row].getPosition()),
                    item
            )
        }
    }

    override fun onMouseMoved(position: Position): Layer? {
        for (row in 0 until lines) {
            if (itemTextBoxes[row].containsPosition(position)) {
                return itemInfoLayers[row]
            }
        }
        return null
    }
}
