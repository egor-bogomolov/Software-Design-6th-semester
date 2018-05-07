package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.LayerBuilder
import org.codetome.zircon.api.builder.TextCharacterBuilder
import org.codetome.zircon.api.builder.TextImageBuilder
import org.codetome.zircon.api.graphics.Layer

internal fun setupHelpLayer(
        position: Position,
        maximumSize: Size
): Layer {
    val text = """Possible actions:
        |I      -> go to inventory
        |T      -> take items from the floor
        |?      -> show this help
        |
        |i      -> change action type to "Interact"
        |m      -> change action type to "Move" (default, resets after each turn)
        |a      -> change action type to "Attack"
        |
        |arrows -> do current action type in a direction pointed by arrow
        |Space  -> do current action type in a current cell
    """.trimMargin()
    val textImage = TextImageBuilder.newBuilder()
            .filler(TextCharacterBuilder.DEFAULT_CHARACTER)
            .size(maximumSize)
            .build()

    for ((row, line) in text.lines().withIndex()) {
        textImage.putText(line, Position.TOP_LEFT_CORNER.withRelativeRow(row))
    }

    return LayerBuilder.newBuilder()
            .textImage(textImage)
            .offset(position)
            .build()
}