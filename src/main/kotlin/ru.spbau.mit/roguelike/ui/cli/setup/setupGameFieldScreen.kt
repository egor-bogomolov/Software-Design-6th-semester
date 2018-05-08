package ru.spbau.mit.roguelike.ui.cli.setup

import kotlinx.coroutines.experimental.runBlocking
import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.input.InputType
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.creatures.*
import ru.spbau.mit.roguelike.map.plus
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameField
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameLog
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameScreenComponent
import ru.spbau.mit.roguelike.ui.cli.setup.field.HeroInfo
import kotlin.coroutines.experimental.suspendCoroutine

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

    val heroInventoryScreen = setupHeroInventoryScreen(
            screen,
            gameRunner
    )

    var actionMode: (Direction) -> DirectedAction = ::Move

    val helpLayer = setupHelpLayer(
            gameField.panel.getPosition().withRelative(Position.OFFSET_1x1),
            gameField.panel.getEffectiveSize()
    )

    screen.ifActiveOnInput { input ->
        val keyStroke by lazy { input.asKeyStroke() }
        if (input.getInputType() == InputType.Character &&
                keyStroke.getCharacter() != ' ') {
            when (keyStroke.getCharacter()) {
                '?' -> {
                    screen.pushLayer(helpLayer)
                    screen.display()
                }
                'I' -> heroInventoryScreen.activate()
                'i' -> actionMode = ::Interact
                'a' -> actionMode = { direction ->
                    runBlocking {
                        val attacked = suspendCoroutine<Creature> {
                            setupAttackTargetDialog(
                                    screen,
                                    gameRunner,
                                    gameRunner.creatureManager.heroPosition + direction,
                                    it
                            ).activate()
                        }
                        Attack(direction, attacked)
                    }
                }
                'm' -> actionMode = ::Move
            }
        } else if (continuation != null) {
            val action = actionMode(
                    when (input.getInputType()) {
                        InputType.ArrowUp    -> Direction.NORTH
                        InputType.ArrowDown  -> Direction.SOUTH
                        InputType.ArrowLeft  -> Direction.WEST
                        InputType.ArrowRight -> Direction.EAST
                        InputType.Escape     -> {
                            screen.removeLayer(helpLayer)
                            screen.display()
                            return@ifActiveOnInput
                        }
                        InputType.Character  ->
                            if (keyStroke.getCharacter() == ' ') {
                                Direction.CURRENT
                            } else {
                                return@ifActiveOnInput
                            }
                        else                 -> return@ifActiveOnInput
                    }
            )
            val copy = continuation
            actionMode = ::Move
            continuation = null
            copy?.resume(action)
            refresh()
        }
    }

    return screen
}
