package ru.spbau.mit.roguelike.ui.cli

import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.resource.CP437TilesetResource
import org.codetome.zircon.api.resource.ColorThemeResource
import ru.spbau.mit.roguelike.creatures.CreatureAction
import ru.spbau.mit.roguelike.creatures.hero.BasicStats
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.items.Equipment
import ru.spbau.mit.roguelike.items.Item
import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.runner.EmptyMapGenerator
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.runner.GameSettings
import ru.spbau.mit.roguelike.runner.NoCreatureGenerator
import ru.spbau.mit.roguelike.ui.GameUI
import ru.spbau.mit.roguelike.ui.cli.setup.setupGameFieldScreen
import ru.spbau.mit.roguelike.ui.cli.setup.setupGameScreen
import ru.spbau.mit.roguelike.ui.cli.setup.setupHeroScreen
import ru.spbau.mit.roguelike.ui.cli.setup.showItemExchangeDialog
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

object CLIGameUI: GameUI(EmptyMapGenerator, NoCreatureGenerator) {
    private val terminalSize = Size.of(80, 40)
    private val font = CP437TilesetResource.WANDERLUST_16X16.toFont()
    private val colorTheme = ColorThemeResource.SOLARIZED_DARK_ORANGE.getTheme()

    internal var continuation: Continuation<CreatureAction>? = null

    internal val terminal = TerminalBuilder
            .newBuilder()
            .title("Roguelike")
            .font(font)
            .initialTerminalSize(terminalSize)
            .build()

    internal class CLIHero(name: String) : Hero(name) {
        init {
            for (i in 1..30) {
                takeItem(
                        Equipment(
                                "Viking helmet $i",
                                "No one know what it is doing here",
                                Equipment.Slot.HELMET,
                                BasicStats()
                        )
                )
            }
        }

        override suspend fun askAction(visibleMap: GameMap) =
                suspendCoroutine<CreatureAction> { continuation = it }

        override suspend fun exchangeItems(items: MutableList<Item>) =
                suspendCoroutine<Unit> {
                    showItemExchangeDialog(items, it)
                }
    }

    override fun setupGame(settingsForwarder: Continuation<GameSettings>) {
        val gameSetupScreen = setupGameScreen(settingsForwarder)
        gameSetupScreen.applyColorTheme(colorTheme)
        gameSetupScreen.display()
    }

    override fun setupHero(heroForwarder: Continuation<Hero>) {
        val heroSetupScreen = setupHeroScreen(heroForwarder)
        heroSetupScreen.applyColorTheme(colorTheme)
        heroSetupScreen.display()
    }

    override fun runGame(
            gameRunner: GameRunner
    ) {
        val gameField = setupGameFieldScreen(gameRunner)
        gameField.applyColorTheme(colorTheme)
        gameField.display()
        while (!gameRunner.gameFinished) {
            gameRunner.nextTurn()
        }
    }

    override fun showResults(
            gameRunner: GameRunner,
            newGameForwarder: Continuation<Boolean>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}