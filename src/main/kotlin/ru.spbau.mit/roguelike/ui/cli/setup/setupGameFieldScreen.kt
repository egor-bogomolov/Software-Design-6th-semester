package ru.spbau.mit.roguelike.ui.cli.setup

import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.Symbols
import org.codetome.zircon.api.builder.*
import org.codetome.zircon.api.color.TextColorFactory
import org.codetome.zircon.api.component.builder.ButtonBuilder
import org.codetome.zircon.api.component.builder.GameComponentBuilder
import org.codetome.zircon.api.component.builder.PanelBuilder
import org.codetome.zircon.api.game.GameArea
import org.codetome.zircon.api.game.Position3D
import org.codetome.zircon.api.game.Size3D
import org.codetome.zircon.api.graphics.TextImage
import org.codetome.zircon.api.input.InputType
import org.codetome.zircon.api.resource.CP437TilesetResource
import org.codetome.zircon.api.resource.ColorThemeResource
import org.codetome.zircon.api.screen.Screen
import org.codetome.zircon.internal.component.impl.DefaultGameComponent
import org.codetome.zircon.internal.graphics.BoxType
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

object GameAreaScrollingWithLayers {

    private val EXIT_CONDITIONS = ArrayList<InputType>()
    private val TERMINAL_WIDTH = 60
    private val TERMINAL_HEIGHT = 30
    private val SIZE = Size.of(TERMINAL_WIDTH, TERMINAL_HEIGHT)
    private var headless = false

    init {
        EXIT_CONDITIONS.add(InputType.Escape)
        EXIT_CONDITIONS.add(InputType.EOF)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // for this example we only need a default terminal (no extra config)
        val terminal = TerminalBuilder
                .newBuilder()
                .font(CP437TilesetResource.ROGUE_YUN_16X16.toFont())
                .initialTerminalSize(SIZE)
                .build()
        if (args.isNotEmpty()) {
            headless = true
        }
        val screen = TerminalBuilder.createScreenFor(terminal)
        screen.setCursorVisibility(false) // we don't want the cursor right now

        val actions = PanelBuilder.newBuilder()
                .size(screen.getBoundableSize().withColumns(20))
                .wrapWithBox()
                .title("Actions")
                .boxType(BoxType.TOP_BOTTOM_DOUBLE)
                .build()
        val wait = ButtonBuilder.newBuilder()
                .text("Wait")
                .build()
        val sleep = ButtonBuilder.newBuilder()
                .text("Sleep")
                .position(Position.DEFAULT_POSITION.withRelativeRow(1))
                .build()
        actions.addComponent(wait)
        actions.addComponent(sleep)
        screen.addComponent(actions)


        val gamePanel = PanelBuilder.newBuilder()
                .size(screen.getBoundableSize().withColumns(40))
                .position(Position.DEFAULT_POSITION.relativeToRightOf(actions))
                .title("Game area")
                .wrapWithBox()
                .boxType(BoxType.TOP_BOTTOM_DOUBLE)
                .build()

        val visibleGameAreaSize = Size3D.from2DSize(gamePanel.getBoundableSize()
                .minus(Size.of(2, 2)), 5)
        val virtualGameAreaSize = Size.of(100, 100)


        val levels = HashMap<Int, List<TextImage>>()
        val totalLevels = 10
        for (i in 0 until totalLevels) {
            levels[i] = listOf(TextImageBuilder.newBuilder()
                    .size(virtualGameAreaSize)
                    .build())
        }

        val gameArea =
                levels.entries.fold(
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

        val gameComponent = GameComponentBuilder.newBuilder()
                .gameArea(gameArea)
                .visibleSize(visibleGameAreaSize)
                .font(CP437TilesetResource.PHOEBUS_16X16.toFont())
                .build()

        screen.addComponent(gamePanel)
        gamePanel.addComponent(gameComponent)

        enableMovement(screen, gameComponent)
        generatePyramid(3, Position3D.of(5, 5, 2), gameArea)
        generatePyramid(6, Position3D.of(15, 9, 5), gameArea)
        generatePyramid(5, Position3D.of(9, 21, 4), gameArea)

        screen.applyColorTheme(ColorThemeResource.SOLARIZED_DARK_CYAN.getTheme())
        screen.display()
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

    private fun enableMovement(screen: Screen, gameComponent: DefaultGameComponent) {
        screen.onInput(Consumer { input ->
            if (EXIT_CONDITIONS.contains(input.getInputType()) && !headless) {
                System.exit(0)
            } else {
                if (InputType.ArrowUp == input.getInputType()) {
                    gameComponent.scrollOneBackward()
                }
                if (InputType.ArrowDown == input.getInputType()) {
                    gameComponent.scrollOneForward()
                }
                if (InputType.ArrowLeft == input.getInputType()) {
                    gameComponent.scrollOneLeft()
                }
                if (InputType.ArrowRight == input.getInputType()) {
                    gameComponent.scrollOneRight()
                }
                if (InputType.PageUp == input.getInputType()) {
                    gameComponent.scrollOneUp()
                }
                if (InputType.PageDown == input.getInputType()) {
                    gameComponent.scrollOneDown()
                }
                screen.drainLayers()
                val visibleOffset = gameComponent.getVisibleOffset()
                screen.pushLayer(LayerBuilder.newBuilder()
                        .textImage(TextCharacterStringBuilder.newBuilder()
                                .backgroundColor(TextColorFactory.TRANSPARENT)
                                .foregroundColor(TextColorFactory.fromString("#aaaadd"))
                                .text(String.format("Position: (x=%s, y=%s, z=%s)", visibleOffset.x, visibleOffset.y, visibleOffset.z))
                                .build()
                                .toTextImage())
                        .offset(Position.of(21, 1))
                        .build())
                screen.refresh()
            }
        })
    }

}

internal fun CLIGameUI.setupGameFieldScreen(gameRunner: GameRunner): Screen {
    TODO("not implemented")
}