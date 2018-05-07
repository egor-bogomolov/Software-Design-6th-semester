package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.component.Panel
import org.codetome.zircon.api.component.TextBox
import org.codetome.zircon.api.component.builder.TextBoxBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.formatEnumValue
import ru.spbau.mit.roguelike.items.Equipment
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.setup.MouseEventHandler
import ru.spbau.mit.roguelike.ui.cli.setup.itemInfoLayer

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

    private val equipmentText: Map<Equipment.Slot,TextBox> =
            Equipment.Slot.values().associate { slot ->
                val textBox = TextBoxBuilder.newBuilder()
                        .size(
                                panel.getEffectiveSize().withRows(1)
                        )
                        .position(
                                Position.of(0, 0)
                                        .withRelativeRow(slot.ordinal)
                        )
                        .build()
                textBox.disable()

                panel.addComponent(textBox)

                textBox.onMouseReleased(MouseEventHandler {
                    if (it.button == 1) {
                        gameRunner.hero.unequipItem(slot)
                        equipmentInfo[slot]?.let(gameScreen::removeLayer)
                        equipmentInfo[slot] = EMPTY_LAYER
                        refreshCallback()
                    }
                })

                return@associate slot to textBox
            }

    private val equipmentInfo: MutableMap<Equipment.Slot,Layer> =
            mutableMapOf()

    init {
        gameScreen.addComponent(panel)
        refresh()
    }

    override fun refresh() {
        for (slot in Equipment.Slot.values()) {
            val slotString = formatEnumValue(slot.name)

            val item = gameRunner.hero.equipment[slot]
            val itemString = item?.name ?: "none"

            val textBox = equipmentText[slot] ?: continue
            equipmentInfo[slot] = if (item != null) {
                itemInfoLayer(
                        panel.getEffectiveSize().columns - 1,
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

    override fun onMouseMoved(position: Position): Layer? {
        for (slot in Equipment.Slot.values()) {
            if (equipmentText[slot]?.containsPosition(position) == true) {
                return equipmentInfo[slot]
            }
        }
        return null
    }
}
