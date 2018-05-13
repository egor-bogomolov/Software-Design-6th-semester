package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.LayerBuilder
import org.codetome.zircon.api.builder.TextImageBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.graphics.TextImage
import org.codetome.zircon.api.resource.CP437TilesetResource
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.runner.GameRunner

/**
 * Internal alias to distinguish map.Position from Zircon's internal Position
 */
private typealias GameMapPosition = ru.spbau.mit.roguelike.map.Position

/**
 * Represents game field
 */
internal class GameField(
        position: Position,
        size: Size,
        gameScreen: Screen,
        gameRunner: GameRunner,
        refreshCallback: () -> Unit
): GameScreenComponent(position, size, gameScreen, gameRunner, refreshCallback) {
    override val panel = panelBuilder
            .title("Game field")
            .build()

    private val imageSize = panel.getEffectiveSize()

    private val fieldTextImage: TextImage = TextImageBuilder.newBuilder()
            .size(imageSize)
            .build()

    /**
     * Position of hero on the screen
     */
    val offset: GameMapPosition
        get() =
            GameMapPosition(
                    imageSize.columns / 2 - gameRunner.creatureManager.heroPosition.first,
                    imageSize.rows / 2 - gameRunner.creatureManager.heroPosition.second
            )

    private var fieldLayer: Layer = constructLayerFromImage()

    private fun constructLayerFromImage(): Layer =
            LayerBuilder.newBuilder()
                    .font(CP437TilesetResource.PHOEBUS_16X16.toFont())
                    .textImage(fieldTextImage)
                    .offset(panel.getPosition().withRelative(Position.OFFSET_1x1))
                    .build()

    init {
        gameScreen.addComponent(panel)
        refresh()
    }

    /**
     * (Re-)loads map to screen
     */
    private fun loadMap() {
        val gameMap: GameMap = gameRunner.heroVisibleMap()

        for (x in 0 until imageSize.columns) {
            for (y in 0 until imageSize.rows) {
                val gameMapPosition = GameMapPosition(
                        x - offset.first,
                        y - offset.second
                )

                fieldTextImage.setCharacterAt(
                        Position.of(x, y),
                        gameMap[gameMapPosition].char
                )
            }
        }
    }

    /**
     * (Re-)loads creatures to screen
     */
    private fun loadCreatures() {
        for ((position, creatures) in gameRunner.creatureManager.creatures) {
            val fieldGameMapPosition = GameMapPosition(
                    position.first + offset.first,
                    position.second + offset.second
            )
            if (fieldGameMapPosition.first < 0 ||
                    fieldGameMapPosition.second < 0) {
                continue
            }
            val fieldPosition = fieldGameMapPosition.let { Position.of(it.first, it.second) }
            if (!fieldTextImage.containsPosition(fieldPosition)) {
                continue
            }
            if (gameRunner.creatureManager.heroPosition == position) {
                fieldTextImage.setCharacterAt(
                        fieldPosition,
                        64.toChar()
                )
            } else if (creatures.isNotEmpty()) {
                fieldTextImage.setCharacterAt(
                        fieldPosition,
                        249.toChar()
                )
            }
        }
    }

    /**
     * Refreshes game field contents
     */
    override fun refresh() {
        loadMap()
        loadCreatures()
        gameScreen.removeLayer(fieldLayer)
        fieldLayer = constructLayerFromImage()
        gameScreen.pushLayer(fieldLayer)
    }
}