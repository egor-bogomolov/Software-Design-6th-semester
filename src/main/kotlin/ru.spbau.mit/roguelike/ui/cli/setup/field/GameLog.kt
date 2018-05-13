package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.component.Panel
import org.codetome.zircon.api.component.TextBox
import org.codetome.zircon.api.component.builder.TextBoxBuilder
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.runner.GameRunner

/**
 * Represents game log which reads messages from @see ru.spbau.mit.roguelike.Logger class
 */
internal class GameLog(
        position: Position,
        size: Size,
        gameScreen: Screen,
        gameRunner: GameRunner,
        refreshCallback: () -> Unit
): GameScreenComponent(position, size, gameScreen, gameRunner, refreshCallback) {
    override val panel: Panel = panelBuilder
            .title("Log")
            .build()

    private val textBox: TextBox = TextBoxBuilder.newBuilder()
            .size(panel.getEffectiveSize())
            .build()

    init {
        gameScreen.addComponent(panel)
        panel.addComponent(textBox)
        textBox.disable()
    }

    /**
     * Refreshes log contents
     */
    override fun refresh() {
        textBox.setText(gameRunner.turnLog.joinToString("\n"))
    }
}