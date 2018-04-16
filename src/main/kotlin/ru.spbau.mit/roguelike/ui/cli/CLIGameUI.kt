package ru.spbau.mit.roguelike.ui.cli

import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.resource.CP437TilesetResource
import org.codetome.zircon.api.resource.ColorThemeResource
import ru.spbau.mit.roguelike.runner.EmptyMapGenerator
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.runner.NoCreatureGenerator
import ru.spbau.mit.roguelike.ui.GameUI
import ru.spbau.mit.roguelike.ui.cli.screens.setupGameScreen

object CLIGameUI: GameUI(EmptyMapGenerator, NoCreatureGenerator) {
    private val terminalSize = Size.of(80, 40)
    private val font = CP437TilesetResource.WANDERLUST_16X16.toFont()
    private val colorTheme = ColorThemeResource.SOLARIZED_DARK_ORANGE.getTheme()

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun runGame(gameRunner: GameRunner) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showResults(gameRunner: GameRunner) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}