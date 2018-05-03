package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.LayerBuilder
import org.codetome.zircon.api.builder.TextCharacterStringBuilder
import org.codetome.zircon.api.color.TextColorFactory
import org.codetome.zircon.api.component.Panel
import org.codetome.zircon.api.component.builder.ButtonBuilder
import org.codetome.zircon.api.component.builder.PanelBuilder
import org.codetome.zircon.api.component.builder.TextBoxBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.input.MouseAction
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.items.Item
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

internal class MouseEventHandler(
        val body: (MouseAction) -> Unit
): Consumer<MouseAction> {
    private var lastMouseEventTime: Long = 0

    private fun checkDelay(currentTime: Long): Boolean =
            if (currentTime < lastMouseEventTime + MOUSE_EVENT_DELAY) {
                false
            } else {
                lastMouseEventTime = currentTime
                true
            }

    override fun accept(mouseAction: MouseAction) {
        if (checkDelay(mouseAction.getEventTime())) {
            body(mouseAction)
        }
    }

    companion object {
        const val MOUSE_EVENT_DELAY: Long = 30
    }
}

internal fun itemInfoLayer(
        position: Position,
        item: Item
): Layer =
        LayerBuilder.newBuilder()
                .offset(position)
                .textImage(
                        TextCharacterStringBuilder.newBuilder()
                                .backgroundColor(TextColorFactory.DEFAULT_BACKGROUND_COLOR)
                                .foregroundColor(TextColorFactory.fromString("#aaaadd"))
                                .text(item.detailedInfo())
                                .build()
                                .toTextImage()
                )
                .build()

internal val panelTemplate = PanelBuilder
        .newBuilder()
        .wrapWithBox()
        .wrapWithShadow()

internal fun setupNumberPanel(
        screen: Screen,
        position: Position,
        title: String,
        minValue: Int,
        maxValue: Int
): Pair<Panel,() -> Int> {
    val valueLength = max(
            minValue.toString().length,
            maxValue.toString().length
    )

    val panelLength = max(
            title.length + 5,
            valueLength + 6
    ) + 1

    val panel = panelTemplate
            .title(title)
            .size(Size.of(panelLength, 4))
            .position(Position.OFFSET_1x1.withRelative(position))
            .build()

    val lessButton = ButtonBuilder
            .newBuilder()
            .text("-")
            .position(Position.of(0, 0)
            )
            .build()

    val valueBox = TextBoxBuilder
            .newBuilder()
            .text(((minValue + maxValue) / 2).toString())
            .position(Position
                    .of(1,0)
                    .relativeToRightOf(lessButton)
            )
            .size(Size.of(valueLength, 1))
            .build()

    valueBox.disable()

    val moreButton = ButtonBuilder
            .newBuilder()
            .text("+")
            .position(Position
                    .of(1, 0)
                    .relativeToRightOf(valueBox)
            )
            .build()

    lessButton.onMouseReleased(Consumer {
        _ ->
        valueBox.setText(
                max(
                        minValue,
                        valueBox.getText().toInt() - 1
                ).toString()
        )
        screen.refresh()
    })

    moreButton.onMouseReleased(Consumer {
        _ ->
        valueBox.setText(
                min(
                        maxValue,
                        valueBox.getText().toInt() + 1
                ).toString()
        )
        screen.refresh()
    })

    panel.addComponent(lessButton)
    panel.addComponent(valueBox)
    panel.addComponent(moreButton)

    return Pair(panel, { valueBox.getText().toInt() })
}