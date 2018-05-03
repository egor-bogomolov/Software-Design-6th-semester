package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.GameAreaBuilder
import org.codetome.zircon.api.builder.TextCharacterBuilder
import org.codetome.zircon.api.builder.TextImageBuilder
import org.codetome.zircon.api.component.builder.GameComponentBuilder
import org.codetome.zircon.api.game.GameArea
import org.codetome.zircon.api.game.Position3D
import org.codetome.zircon.api.game.Size3D
import org.codetome.zircon.api.resource.CP437TilesetResource
import org.codetome.zircon.api.screen.Screen
import org.codetome.zircon.internal.font.impl.PickRandomMetaStrategy
import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.runner.GameRunner

typealias GameMapPosition = ru.spbau.mit.roguelike.map.Position

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

    private val visibleGameAreaSize = Size3D.from2DSize(
            panel.getEffectiveSize(),
            1
    )

    private val gameMapSize: Size = gameRunner.settings.mapDimensions.let {
        Size.of(it.first, it.second)
    }

    private val virtualGameAreaSize: Size = panel.getEffectiveSize() +
            gameMapSize

    private val gameMapOffset = panel.getEffectiveSize().let {
        Position.of(it.columns / 2, it.rows / 2)
    }

    private val gameArea = setupGameArea()

    private fun setupGameArea(): GameArea {
        val level = TextImageBuilder.newBuilder()
                .size(virtualGameAreaSize)
                .build()

        return GameAreaBuilder.newBuilder()
                .size(
                        Size3D.from2DSize(
                                virtualGameAreaSize,
                                1
                        )
                )
                .setLevel(0, level)
                .build()
    }

    val gameComponent = GameComponentBuilder.newBuilder()
            .gameArea(gameArea)
            .visibleSize(visibleGameAreaSize)
            .font(CP437TilesetResource.ROGUE_YUN_16X16.toFont())
            .build()

    init {
        gameScreen.addComponent(panel)
        panel.addComponent(gameComponent)

        gameComponent.scrollRightBy(gameMapOffset.column)
        gameComponent.scrollForwardBy(gameMapOffset.row)
    }

    private fun Position.toGameMapPosition(): GameMapPosition =
            Pair(
                    column - gameMapOffset.column,
                    row - gameMapOffset.row
            )

    private fun fromGameMapPosition(gameMapPosition: GameMapPosition): Position3D =
            Position3D.from2DPosition(
                    Position.of(
                            gameMapPosition.first + gameMapOffset.column,
                            gameMapPosition.second + gameMapOffset.row
                    ),
                    0
            )

    private fun loadMap() {
        val gameMap: GameMap = gameRunner.heroVisibleMap()

        val metaStrategy = PickRandomMetaStrategy()

        for (x in 0 until virtualGameAreaSize.columns) {
            for (y in 0 until virtualGameAreaSize.rows) {
                val gameAreaPosition =
                        Position3D.from2DPosition(
                                Position.of(x, y),
                                0
                        )
                val gameMapPosition = Position.of(x, y).toGameMapPosition()

                val char = gameMap[gameMapPosition].char

                gameArea.setCharacterAt(
                        gameAreaPosition,
                        0,
                        TextCharacterBuilder
                                .newBuilder()
                                .character(char)
                                .build()
                )
            }
        }
    }

    private fun loadCreatures() {
        for ((position, creatures) in gameRunner.creatureManager.creatures) {
            if (gameRunner.creatureManager.heroPosition == position) {
                gameArea.setCharacterAt(
                        fromGameMapPosition(position),
                        0,
                        TextCharacterBuilder
                                .newBuilder()
                                .character(64.toChar())
                                .build()
                )
            } else if (creatures.isNotEmpty()) {
                gameArea.setCharacterAt(
                        fromGameMapPosition(position),
                        0,
                        TextCharacterBuilder
                                .newBuilder()
                                .character(159.toChar())
                                .build()
                )
            }
        }
    }

    override fun refresh() {
        loadMap()
        loadCreatures()
    }
}