package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.Symbols
import org.codetome.zircon.api.builder.LayerBuilder
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.builder.TextCharacterBuilder
import org.codetome.zircon.api.builder.TextCharacterStringBuilder
import org.codetome.zircon.api.color.TextColorFactory
import org.codetome.zircon.api.game.GameArea
import org.codetome.zircon.api.game.Position3D
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.input.InputType
import org.codetome.zircon.api.screen.Screen
import org.codetome.zircon.internal.component.impl.DefaultGameComponent
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import ru.spbau.mit.roguelike.ui.cli.setup.field.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

fun CLIGameUI.setupGameFieldScreen(gameRunner: GameRunner): Screen {
    val screen = TerminalBuilder.createScreenFor(terminal)
    screen.setCursorVisibility(false) // we don't want the cursor right now

    val components: MutableList<GameScreenComponent> = mutableListOf()

    val refresh = {
        for (component in components) {
            component.refresh()
        }
        screen.display()
    }

    val heroInfo = HeroInfo(
            Position.DEFAULT_POSITION,
            Size.of(
                    23,
                    8
            ),
            screen,
            gameRunner,
            refresh
    )

    components += heroInfo

    val heroEquipment = HeroEquipment(
            Position.of(0, 0).relativeToBottomOf(heroInfo.panel),
            heroInfo.panel.getBoundableSize().withRows(8),
            screen,
            gameRunner,
            refresh
    )

    components += heroEquipment

    val heroInventory = HeroInventory(
            Position.of(0, 0).relativeToBottomOf(heroEquipment.panel),
            heroInfo.panel.getBoundableSize().withRows(
                    screen.getBoundableSize().rows -
                            heroEquipment.panel.getPosition().row -
                            heroEquipment.panel.getBoundableSize().rows
            ),
            screen,
            gameRunner,
            refresh
    )

    components += heroInventory

    val gameField = GameField(
            Position
                    .of(0, 0)
                    .relativeToRightOf(heroInfo.panel),
            screen
                    .getBoundableSize()
                    .withRelativeColumns(
                            -heroInfo.panel.getBoundableSize().columns
                    ),
            screen,
            gameRunner,
            refresh
    )

    components += gameField

    enableMovement(screen, gameField.gameComponent, gameRunner)
    generatePyramid(3, Position3D.of(5, 5, 2), gameField.gameArea)
    generatePyramid(6, Position3D.of(15, 9, 5), gameField.gameArea)
    generatePyramid(5, Position3D.of(9, 21, 4), gameField.gameArea)

    return screen
}

private fun generatePyramid(height: Int, startPos: Position3D, gameArea: GameArea) {
    val percent = 1.0 / (height + 1)
    val wall = TextCharacterBuilder.newBuilder()
            .character(Symbols.BLOCK_SOLID)
            .build()
    val currLevel = AtomicInteger(startPos.z)
    for (currSize in 0 until height) {
        val currPercent = (currSize + 1) * percent
        val levelOffset = startPos.to2DPosition()
                .withRelativeColumn(-currSize)
                .withRelativeRow(-currSize)
        val levelSize = Size.of(1 + currSize * 2, 1 + currSize * 2)
        currLevel.decrementAndGet()
    }
}

private fun enableMovement(
        screen: Screen,
        gameComponent: DefaultGameComponent,
        gameRunner: GameRunner
) {
    val visibleOffset = gameComponent.getVisibleOffset()

    fun buildCoordinateLayer(): Layer =
            LayerBuilder.newBuilder()
                    .textImage(TextCharacterStringBuilder.newBuilder()
                            .backgroundColor(TextColorFactory.TRANSPARENT)
                            .foregroundColor(TextColorFactory.fromString("#aaaadd"))
                            .text(String.format(
                                    "Position: (x=%s, y=%s)",
                                    visibleOffset.x,
                                    visibleOffset.y
                            ))
                            .build()
                            .toTextImage()
                    )
                    .offset(Position.of(24, 1))
                    .build()

    var coordinateLayer: Layer = buildCoordinateLayer()

    screen.onInput(Consumer { input ->
        when (input.getInputType()) {
            InputType.ArrowUp    -> gameComponent.scrollOneBackward()
            InputType.ArrowDown  -> gameComponent.scrollOneForward()
            InputType.ArrowLeft  -> gameComponent.scrollOneLeft()
            InputType.ArrowRight -> gameComponent.scrollOneRight()
            else                 -> {} // no action
        }
        screen.removeLayer(coordinateLayer)
        coordinateLayer = buildCoordinateLayer()
        screen.pushLayer(coordinateLayer)
        screen.display()
    })
}