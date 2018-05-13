package ru.spbau.mit.roguelike.ui.cli.setup.field

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.Size
import org.codetome.zircon.api.component.TextBox
import org.codetome.zircon.api.component.builder.TextBoxBuilder
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.creatures.hero.BasicStats
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.runner.GameRunner

/**
 * Represents hero info game component
 */
internal class HeroInfo(
        position: Position,
        size: Size,
        gameScreen: Screen,
        gameRunner: GameRunner,
        refreshCallback: () -> Unit
): GameScreenComponent(position, size, gameScreen, gameRunner, refreshCallback) {
    override val panel = panelBuilder
            .title("Hero")
            .build()

    var statsText: TextBox

    init {
        gameScreen.addComponent(panel)

        statsText = TextBoxBuilder.newBuilder()
                .size(panel.getEffectiveSize())
                .text(heroInfoText(gameRunner.hero))
                .build()

        statsText.disable()

        panel.addComponent(statsText)
    }

    override fun refresh() {
        statsText.setText(heroInfoText(gameRunner.hero))
    }

    private fun heroInfoText(hero: Hero): String =
            StringBuilder()
                    .appendln("Name: ${hero.name}")
                    .appendln(
                            "Health: ${hero.health}/${hero.maxHealth}"
                    )
                    .appendln("Evasion: ${hero.dodgeChance * 100}%")
                    .appendln(
                            BasicStats.Type.values()
                                    .joinToString("\n") {
                                        "${it
                                                .name
                                                .toLowerCase()
                                                .capitalize()}: ${hero.totalStats[it]}"
                                    }
                    )
                    .toString()
}