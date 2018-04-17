package ru.spbau.mit.roguelike.ui.cli.screens

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.component.builder.ButtonBuilder
import org.codetome.zircon.api.component.builder.LabelBuilder
import org.codetome.zircon.api.component.builder.RadioButtonGroupBuilder
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.runner.GameSettings
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import java.util.function.Consumer
import kotlin.math.max

internal fun CLIGameUI.setupGameScreen(): Screen {
    val screen = TerminalBuilder.createScreenFor(terminal)

    val gameSetupLabel = LabelBuilder
            .newBuilder()
            .text("Game setup:")
            .position(Position.OFFSET_1x1)
            .build()

    screen.addComponent(gameSetupLabel)

    val (widthPanel, widthGetter) = setupNumberPanel(
            screen,
            Position
                    .of(0, 0)
                    .relativeToBottomOf(gameSetupLabel),
            "Map width",
            10,
            100
    )

    screen.addComponent(widthPanel)

    val (heightPanel, heightGetter) = setupNumberPanel(
            screen,
            Position
                    .of(0, 0)
                    .relativeToRightOf(widthPanel)
                    .withRelativeRow(-1),
            "Map height",
            10,
            100
    )

    screen.addComponent(heightPanel)

    val difficultyTitle = "Difficulty (default=NORMAL)"

    val difficultyPanel = panelTemplate
            .title(difficultyTitle)
            .size(Size.of(
                    max(
                            difficultyTitle.length + 5,
                            GameSettings.Difficulty.values().map { it.toString().length }.max() ?: -1 + 4
                    ),
                    3 + GameSettings.Difficulty.values().size
            ))
            .position(Position
                    .TOP_LEFT_CORNER
                    .relativeToBottomOf(widthPanel)
                    .withRelativeRow(1)
            )
            .build()

    val difficultySetter = RadioButtonGroupBuilder
            .newBuilder()
            .position(Position
                    .of(0, 0)
            )
            .size(Size.of(
                    GameSettings.Difficulty.values().map { it.toString().length + 4 }.max() ?: -1,
                    GameSettings.Difficulty.values().size
            ))
            .build()

    for (option in GameSettings.Difficulty.values()) {
        val optionName = option.toString()

        difficultySetter.addOption(
                optionName,
                optionName
        )
    }

    difficultyPanel.addComponent(difficultySetter)

    screen.addComponent(difficultyPanel)

    val continueButton = ButtonBuilder
            .newBuilder()
            .text("Continue")
            .position(Position
                    .of(0, 1)
                    .relativeToBottomOf(difficultyPanel)
            )
            .build()

    continueButton.onMouseReleased(Consumer {
        _ ->
        this.gameSettings = GameSettings(
                Pair(
                        widthGetter(),
                        heightGetter()
                ),
                GameSettings.Difficulty.valueOf(
                        difficultySetter
                                .getSelectedOption()
                                .orElse(GameSettings.Difficulty.NORMAL.toString())
                )
        )
        this.setupHero()
    })

    screen.addComponent(continueButton)

    return screen
}