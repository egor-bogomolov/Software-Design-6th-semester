package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.component.Panel
import org.codetome.zircon.api.component.TextBox
import org.codetome.zircon.api.component.builder.TextBoxBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.input.MouseActionType
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.setup.itemInfoLayer
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

internal class HeroInventory(
        position: Position,
        size: Size,
        gameScreen: Screen,
        gameRunner: GameRunner
): GameScreenComponent(position, size, gameScreen, gameRunner) {
    override val panel: Panel = panelBuilder
            .title("Inventory")
            .build()

    private val lines: Int = size.rows - 2
    private var scroll: Int = 0
    private var inventory: List<Pair<TextBox,Layer>> =
            emptyList()

    init {
        gameScreen.addComponent(panel)

        panel.onMouseReleased(Consumer {
            when (it.actionType) {
                MouseActionType.MOUSE_WHEEL_ROTATED_DOWN ->
                    scroll = min(
                            scroll + 1,
                            gameRunner.hero.backpack.size - lines
                    )
                MouseActionType.MOUSE_WHEEL_ROTATED_UP ->
                    scroll = max(
                            scroll - 1,
                            0
                    )
                else -> {} // no action
            }
            refresh()
            gameScreen.refresh()
        })

        refresh()
    }

    override fun refresh() {
        inventory.forEach {
            (component, layer) ->
            panel.removeComponent(component)
            gameScreen.removeLayer(layer)
        }
        inventory = gameRunner.hero.backpack.mapIndexed { index, item ->
            val textBox = TextBoxBuilder.newBuilder()
                    .size(
                            panel.getEffectiveSize().withRows(1)
                    )
                    .position(
                            Position.of(0, 0)
                                    .withRelativeRow(index)
                    )
                    .text(item.name)
                    .build()

            panel.addComponent(textBox)
            textBox.disable()

            val infoLayer = itemInfoLayer(
                    Position.OFFSET_1x1
                            .withRelative(textBox.getPosition()),
                    item
            )
            textBox.onMouseMoved(Consumer {
                when (it.actionType) {
                    MouseActionType.MOUSE_ENTERED ->  {
                        gameScreen.pushLayer(infoLayer)
                    }
                    MouseActionType.MOUSE_EXITED -> {
                        gameScreen.removeLayer(infoLayer)
                    }
                    else -> {} // no action
                }
                gameScreen.refresh()
            })
            textBox.onMousePressed(Consumer {
                when (it.button) {
                    0 -> gameRunner.hero.equipItem(index)
                    1 -> gameRunner.hero.dropItem(index) // TODO("implement drop to map cell")
                }
                refresh()
                gameScreen.refresh()
            })

            return@mapIndexed Pair(textBox,infoLayer)
        }
    }
}
