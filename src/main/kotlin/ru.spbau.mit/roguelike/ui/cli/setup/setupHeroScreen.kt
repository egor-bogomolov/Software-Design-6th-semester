package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.component.builder.ButtonBuilder
import org.codetome.zircon.api.component.builder.LabelBuilder
import org.codetome.zircon.api.component.builder.TextBoxBuilder
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import java.util.function.Consumer
import kotlin.coroutines.experimental.Continuation

internal fun CLIGameUI.setupHeroScreen(
        heroForwarder: Continuation<Hero>
): Screen {
    val screen = TerminalBuilder.createScreenFor(terminal)

    val heroSetupLabel = LabelBuilder
            .newBuilder()
            .text("Hero setup:")
            .position(Position.OFFSET_1x1)
            .build()

    screen.addComponent(heroSetupLabel)

    val namePanel = panelTemplate
            .title("Hero name")
            .position(Position
                    .of(0, 0)
                    .relativeToBottomOf(heroSetupLabel)
            )
            .size(Size.of(screen.getBoundableSize().columns / 2, 4))
            .build()

    screen.addComponent(namePanel)

    val nameBox = TextBoxBuilder
            .newBuilder()
            .position(Position.of(0, 0))
            .size(Size.of(namePanel.getBoundableSize().columns - 3, 1))
            .text("Elbereth")
            .build()

    namePanel.addComponent(nameBox)

    val continueButton = ButtonBuilder
            .newBuilder()
            .text("Continue")
            .position(Position
                    .of(0, 1)
                    .relativeToBottomOf(namePanel)
            )
            .build()

    var resumed = false
    continueButton.onMouseReleased(Consumer {
        if (!resumed) {
            heroForwarder.resume(CLIGameUI.CLIHero(nameBox.getText()))
            resumed = true
        }
    })

    screen.addComponent(continueButton)

    return screen
}