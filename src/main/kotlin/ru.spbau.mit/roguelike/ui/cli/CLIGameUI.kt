package ru.spbau.mit.roguelike.ui.cli

import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.resource.CP437TilesetResource
import org.codetome.zircon.api.resource.ColorThemeResource
import ru.spbau.mit.roguelike.creatures.CreatureAction
import ru.spbau.mit.roguelike.runner.EmptyMapGenerator
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.runner.NoCreatureGenerator
import ru.spbau.mit.roguelike.ui.GameUI
import ru.spbau.mit.roguelike.ui.cli.setup.setupGameFieldScreen
import ru.spbau.mit.roguelike.ui.cli.setup.setupGameScreen
import ru.spbau.mit.roguelike.ui.cli.setup.setupHeroScreen
import kotlin.coroutines.experimental.Continuation

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

    override fun setupGame() {
        val gameSetupScreen = setupGameScreen()
        gameSetupScreen.applyColorTheme(colorTheme)
        gameSetupScreen.display()
    }

    override fun setupHero() {
        val heroSetupScreen = setupHeroScreen()
        heroSetupScreen.applyColorTheme(colorTheme)
        heroSetupScreen.display()
    }

    override fun runGame(gameRunner: GameRunner) {
        val gameField = setupGameFieldScreen(gameRunner)
        gameField.applyColorTheme(colorTheme)
        gameField.display()
        while (!gameRunner.gameFinished) {
            gameRunner.nextTurn()
        }
        showResults(gameRunner)
    }

    override fun showResults(gameRunner: GameRunner) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}