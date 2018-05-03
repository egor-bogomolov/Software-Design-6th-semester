package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.LayerBuilder
import org.codetome.zircon.api.component.Panel
import org.codetome.zircon.api.component.TextBox
import org.codetome.zircon.api.component.builder.TextBoxBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.input.MouseActionType
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.formatEnumValue
import ru.spbau.mit.roguelike.items.Equipment
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.setup.itemInfoLayer
import java.util.function.Consumer

internal class HeroEquipment(
        position: Position,
        size: Size,
        gameScreen: Screen,
        gameRunner: GameRunner,
        refreshCallback: () -> Unit
): GameScreenComponent(position, size, gameScreen, gameRunner, refreshCallback) {
    override val panel: Panel = panelBuilder
            .title("Equipment")
            .build()

    private val equipmentText: MutableMap<Equipment.Slot,TextBox> =
            mutableMapOf()
    private val equipmentInfo: MutableMap<Equipment.Slot,Layer> =
            mutableMapOf()

    init {
        gameScreen.addComponent(panel)

        for (slot in Equipment.Slot.values()) {
            val slotString = formatEnumValue(slot.name)

            val item = gameRunner.hero.equipment[slot]
            val itemString = item?.name ?: "none"

            val textBox = TextBoxBuilder.newBuilder()
                    .size(
                            panel.getEffectiveSize().withRows(1)
                    )
                    .position(
                            Position.of(0, 0)
                                    .withRelativeRow(slot.ordinal)
                    )
                    .text("$slotString: $itemString")
                    .build()

            panel.addComponent(textBox)
            textBox.disable()
            equipmentInfo[slot] = if (item != null) {
                itemInfoLayer(
                        Position.OFFSET_1x1
                                .withRelative(textBox.getPosition()),
                        item
                )
            } else {
                EMPTY_LAYER
            }
            textBox.onMouseMoved(Consumer {
                when (it.actionType) {
                    MouseActionType.MOUSE_ENTERED -> equipmentInfo[slot]?.let {
                        gameScreen.pushLayer(it)
                    }
                    MouseActionType.MOUSE_EXITED -> equipmentInfo[slot]?.let {
                        gameScreen.removeLayer(it)
                    }
                    else -> {} // no action
                }
                refreshCallback()
            })
            textBox.onMouseReleased(Consumer {
                gameRunner.hero.unequipItem(slot)
                equipmentInfo[slot]?.let {
                    gameScreen.removeLayer(it)
                }
                equipmentInfo[slot] = EMPTY_LAYER
                refreshCallback()
            })
            equipmentText[slot] = textBox
        }
    }

    override fun refresh() {
        for (slot in Equipment.Slot.values()) {
            val slotString = formatEnumValue(slot.name)

            val item = gameRunner.hero.equipment[slot]
            val itemString = item?.name ?: "none"

            val textBox = equipmentText[slot]
            equipmentInfo[slot] = if (item != null && textBox != null) {
                itemInfoLayer(
                        Position.OFFSET_1x1
                                .withRelative(textBox.getPosition()),
                        item
                )
            } else {
                EMPTY_LAYER
            }

            equipmentText[slot]?.setText("$slotString: $itemString")
        }
    }

    companion object {
        val EMPTY_LAYER: Layer = LayerBuilder.newBuilder().build()
    }
}
