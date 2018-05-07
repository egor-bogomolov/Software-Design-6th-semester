package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.input.InputType
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.creatures.Direction
import ru.spbau.mit.roguelike.creatures.Move
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameField
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameLog
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameScreenComponent
import ru.spbau.mit.roguelike.ui.cli.setup.field.HeroInfo
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

    screen.onInput(Consumer { input ->
        val keyStroke by lazy { input.asKeyStroke() }
        if (input.getInputType() == InputType.Character &&
                keyStroke.getCharacter() == 'I') {
            setupHeroInventoryScreen(
                    screen,
                    gameRunner
            ).display()
        } else if (continuation != null) {
            val action = when (input.getInputType()) {
                InputType.ArrowUp    -> Move(Direction.NORTH)
                InputType.ArrowDown  -> Move(Direction.SOUTH)
                InputType.ArrowLeft  -> Move(Direction.WEST)
                InputType.ArrowRight -> Move(Direction.EAST)
                else                 -> return@Consumer
            }
            val copy = continuation
            continuation = null
            copy?.resume(action)
            refresh()
        }
    })

    return screen
}