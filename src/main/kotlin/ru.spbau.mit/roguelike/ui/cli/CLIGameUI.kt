package ru.spbau.mit.roguelike.ui.cli

import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.resource.CP437TilesetResource
import org.codetome.zircon.api.resource.ColorThemeResource
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.creatures.Creature
import ru.spbau.mit.roguelike.creatures.CreatureAction
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.items.Item
import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.map.Position
import ru.spbau.mit.roguelike.runner.EmptyMapGenerator
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.runner.GameSettings
import ru.spbau.mit.roguelike.runner.NGoblinsGenerator
import ru.spbau.mit.roguelike.ui.GameUI
import ru.spbau.mit.roguelike.ui.cli.setup.*
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

internal val terminalColorTheme =
        ColorThemeResource.SOLARIZED_DARK_ORANGE.getTheme()

/**
 * CLI GameUI implementation
 */
object CLIGameUI: GameUI(EmptyMapGenerator, NGoblinsGenerator(5)) {
    private val terminalSize = Size.of(80, 40)

    internal var continuation: Continuation<CreatureAction>? = null

    internal val terminal = TerminalBuilder
            .newBuilder()
            .title("Roguelike")
            .font(CP437TilesetResource.WANDERLUST_16X16.toFont())
            .initialTerminalSize(terminalSize)
            .build()

    private lateinit var gameField: Screen

    internal class CLIHero(name: String) : Hero(name) {
        override suspend fun askAction(
                position: Position,
                visibleMap: GameMap,
                visibleCreatures: Map<Position, Set<Creature>>
        ) = suspendCoroutine<CreatureAction> { continuation = it }

        override fun exchangeItems(items: MutableList<Item>) {
            val exchangeDialog = setupItemExchangeDialog(
                    items,
                    gameField,
                    gameRunner
            )
            exchangeDialog.applyColorTheme(terminalColorTheme)
            exchangeDialog.activate()
        }
    }

    override fun setupGame(settingsForwarder: Continuation<GameSettings>) {
        val gameSetupScreen = setupGameScreen(settingsForwarder)
        gameSetupScreen.applyColorTheme(terminalColorTheme)
        gameSetupScreen.activate()
    }

    override fun setupHero(heroForwarder: Continuation<Hero>) {
        val heroSetupScreen = setupHeroScreen(heroForwarder)
        heroSetupScreen.applyColorTheme(terminalColorTheme)
        heroSetupScreen.activate()
    }

    override fun runGame(
            gameRunner: GameRunner
    ) {
        gameField = setupGameFieldScreen(gameRunner)
        gameField.applyColorTheme(terminalColorTheme)
        gameField.activate()
        while (!gameRunner.gameFinished) {
            gameRunner.nextTurn()
        }
    }

    override fun showResults(
            gameRunner: GameRunner,
            newGameForwarder: Continuation<Boolean>
    ) {
        val resultsScreen = setupResultsScreen(gameRunner, newGameForwarder)
        resultsScreen.applyColorTheme(terminalColorTheme)
        resultsScreen.activate()
    }
}