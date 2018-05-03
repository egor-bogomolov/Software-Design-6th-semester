package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.LayerBuilder
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.builder.TextCharacterStringBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.input.InputType
import org.codetome.zircon.api.screen.Screen
import org.codetome.zircon.internal.component.impl.DefaultGameComponent
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameField
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameLog
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameScreenComponent
import ru.spbau.mit.roguelike.ui.cli.setup.field.HeroInfo
import java.util.function.Consumer

var refresh: () -> Unit = {}

fun CLIGameUI.setupGameFieldScreen(gameRunner: GameRunner): Screen {
    val screen = TerminalBuilder.createScreenFor(terminal)
    screen.setCursorVisibility(false) // we don't want the cursor right now

    val components: MutableList<GameScreenComponent> = mutableListOf()

    refresh = {
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

//    val heroEquipment = HeroEquipment(
//            Position.of(0, 0).relativeToBottomOf(heroInfo.panel),
//            heroInfo.panel.getBoundableSize().withRows(8),
//            screen,
//            gameRunner,
//            refresh
//    )
//
//    components += heroEquipment
//
//    val heroInventory = HeroInventory(
//            Position.of(0, 0).relativeToBottomOf(heroEquipment.panel),
//            heroInfo.panel.getBoundableSize().withRows(
//                    screen.getBoundableSize().rows -
//                            heroEquipment.panel.getPosition().row -
//                            heroEquipment.panel.getBoundableSize().rows
//            ),
//            screen,
//            gameRunner,
//            refresh
//    )
//
//    components += heroInventory

    val gameLog = GameLog(
            Position.of(0, 0)
                    .relativeToRightOf(heroInfo.panel),
            screen
                    .getBoundableSize()
                    .withRelativeColumns(
                            -heroInfo.panel.getBoundableSize().columns
                    )
                    .withRows(
                            heroInfo.panel.getBoundableSize().rows
                    ),
            screen,
            gameRunner,
            refresh
    )

    components += gameLog

    val gameField = GameField(
            Position
                    .of(0, 0)
                    .relativeToBottomOf(heroInfo.panel),
            screen
                    .getBoundableSize()
                    .withRelativeRows(
                            -heroInfo.panel.getBoundableSize().rows
                    ),
            screen,
            gameRunner,
            refresh
    )

    components += gameField

    enableMovement(screen, gameField.gameComponent)

    return screen
}

private fun enableMovement(
        screen: Screen,
        gameComponent: DefaultGameComponent
) {
    fun buildCoordinateLayer(): Layer {
        val visibleOffset = gameComponent.getVisibleOffset()

        return LayerBuilder.newBuilder()
                .textImage(TextCharacterStringBuilder.newBuilder()
                        .text(String.format(
                                "Position: (x=%s, y=%s)",
                                visibleOffset.x,
                                visibleOffset.y
                        ))
                        .build()
                        .toTextImage()
                )
                .offset(screen
                        .getBoundableSize()
                        .fetchBottomLeftPosition()
                        .withRelativeRow(-1)
                        .withRelativeColumn(1)
                )
                .build()
    }

    var coordinateLayer: Layer = buildCoordinateLayer()
    screen.pushLayer(coordinateLayer)

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
        refresh()
    })
}