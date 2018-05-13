package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.input.InputType
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.creatures.*
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.map.PassableCell
import ru.spbau.mit.roguelike.map.plus
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameField
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameLog
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameScreenComponent
import ru.spbau.mit.roguelike.ui.cli.setup.field.HeroInfo
import ru.spbau.mit.roguelike.ui.cli.terminalColorTheme
import kotlin.coroutines.experimental.Continuation

/**
 * Sets up game field to run game onto
 * @param gameRunner to get information from
 * @return constructed screen
 */
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
    heroInventoryScreen.applyColorTheme(terminalColorTheme)

    var actionMode = ActionMode.MOVE

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
                'T' -> {
                    val heroCell = gameRunner.gameMap[gameRunner.creatureManager.heroPosition]
                    gameRunner.hero.exchangeItems(
                            (heroCell as PassableCell).lyingItems
                    )
                }
                'I' -> heroInventoryScreen.activate()
                'i' -> actionMode = ActionMode.INTERACT
                'a' -> actionMode = ActionMode.ATTACK
                'm' -> actionMode = ActionMode.MOVE
            }
        } else if (continuation != null) {
            val direction =
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
            val copy = continuation
            continuation = null
            processAction(
                    screen,
                    gameRunner,
                    direction,
                    actionMode,
                    copy!!,
                    refresh
            )
            actionMode = ActionMode.MOVE
        }
    }

    return screen
}

/**
 * CLIGameUI hero action processor function
 * @param screen to process action on
 * @param gameRunner to get game info from
 * @param direction of the action
 * @param actionMode of the action
 * @param continuation to send constructed action to
 * @param screen contents refreshing function
 */
private fun CLIGameUI.processAction(
        screen: Screen,
        gameRunner: GameRunner,
        direction: Direction,
        actionMode: ActionMode,
        continuation: Continuation<CreatureAction>,
        refresh: () -> Unit
) {
    val resume = { creatureAction: CreatureAction ->
        continuation.resume(creatureAction)
        refresh()
    }

    when (actionMode) {
        ActionMode.MOVE     -> resume(Move(direction))
        ActionMode.INTERACT -> resume(Interact(direction))
        ActionMode.ATTACK   -> {
            val attackedPosition = gameRunner.creatureManager.heroPosition + direction
            val possibleTargets = gameRunner.creatureManager[attackedPosition].minus(gameRunner.hero)
            when {
                possibleTargets.isEmpty()        -> resume(PassTurn)
                possibleTargets.size > 1         -> {
                    val attackDialog = setupAttackTargetDialog(
                            direction,
                            possibleTargets,
                            resume,
                            screen
                    )
                    attackDialog.applyColorTheme(terminalColorTheme)
                    attackDialog.activate()
                }
                possibleTargets.single() is Hero -> resume(PassTurn)
                else                             -> resume(Attack(direction, possibleTargets.single()))
            }
        }
    }
}

/**
 * Represents current action mode
 */
private enum class ActionMode {
    MOVE,
    ATTACK,
    INTERACT
}