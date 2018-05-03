package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.GameAreaBuilder
import org.codetome.zircon.api.builder.TextImageBuilder
import org.codetome.zircon.api.component.builder.GameComponentBuilder
import org.codetome.zircon.api.game.GameArea
import org.codetome.zircon.api.game.Size3D
import org.codetome.zircon.api.graphics.TextImage
import org.codetome.zircon.api.resource.CP437TilesetResource
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.runner.GameRunner
import java.util.*

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

    val gameArea = setupGameArea()

    val visibleGameAreaSize = Size3D.from2DSize(panel.getBoundableSize()
            .minus(Size.of(2, 2)), 5)

    private fun setupGameArea(): GameArea {
        val virtualGameAreaSize = Size.of(100, 100)

        val levels = HashMap<Int, List<TextImage>>()
        val totalLevels = 10
        for (i in 0 until totalLevels) {
            levels[i] = listOf(TextImageBuilder.newBuilder()
                    .size(virtualGameAreaSize)
                    .build())
        }

        return levels.entries.fold(
                GameAreaBuilder
                        .newBuilder()
                        .size(Size3D.from2DSize(
                                virtualGameAreaSize,
                                totalLevels
                        ))
        ) {
            builder, (index, level) ->
            builder.setLevel(index, level)
        }
                .build()
    }

    val gameComponent = GameComponentBuilder.newBuilder()
            .gameArea(gameArea)
            .visibleSize(visibleGameAreaSize)
            .font(CP437TilesetResource.PHOEBUS_16X16.toFont())
            .build()

    init {
        gameScreen.addComponent(panel)
        panel.addComponent(gameComponent)
    }

    override fun refresh() {
    }
}