package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.component.builder.ButtonBuilder
import org.codetome.zircon.api.component.builder.LabelBuilder
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import java.util.function.Consumer
import kotlin.coroutines.experimental.Continuation

/**
 * @see ru.spbau.mit.roguelike.ui.GameUI.showResults
 */
internal fun CLIGameUI.setupResultsScreen(
        gameRunner: GameRunner,
        newGameForwarder: Continuation<Boolean>
): Screen {
    val screen = TerminalBuilder.createScreenFor(terminal)

    val text = if (gameRunner.creatureManager.heroAlive) {
        "You won!"
    } else {
        "You died"
    }

    val screenSize = screen.getBoundableSize()

    val label = LabelBuilder.newBuilder()
            .text(text)
            .position(Position.of(
                    screenSize.columns / 2 - text.length / 2,
                    screenSize.rows / 2
            ))
            .build()

    screen.addComponent(label)

    val retryText = "Retry"

    val retryButton = ButtonBuilder.newBuilder()
            .text(retryText)
            .position(screenSize.fetchBottomLeftPosition())
            .build()

    screen.addComponent(retryButton)

    var resumed = false

    retryButton.onMouseReleased(Consumer {
        if (!resumed) {
            newGameForwarder.resume(true)
            resumed = true
        }
    })

    val exitText = "Exit"

    val exitButton = ButtonBuilder.newBuilder()
            .text(exitText)
            .position(screenSize
                    .fetchBottomRightPosition()
                    .withRelativeColumn(-exitText.length - 1)
            )
            .build()

    screen.addComponent(exitButton)

    exitButton.onMouseReleased(Consumer {
        if (!resumed) {
            newGameForwarder.resume(false)
            resumed = true
            terminal.close()
        }
    })

    return screen
}