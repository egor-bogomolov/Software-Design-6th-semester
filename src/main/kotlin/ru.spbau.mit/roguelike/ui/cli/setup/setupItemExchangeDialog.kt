package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Position
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.input.InputType
import org.codetome.zircon.api.input.MouseActionType
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.items.Item
import ru.spbau.mit.roguelike.runner.GameRunner
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import ru.spbau.mit.roguelike.ui.cli.setup.field.GameScreenComponent
import ru.spbau.mit.roguelike.ui.cli.setup.field.ItemViewer

/**
 * @see ru.spbau.mit.roguelike.creatures.hero.Hero.exchangeItems
 */
internal fun CLIGameUI.setupItemExchangeDialog(
        items: MutableList<Item>,
        returnToScreen: Screen,
        gameRunner: GameRunner
): Screen {
    val screen = TerminalBuilder.createScreenFor(terminal)

    val components: MutableList<GameScreenComponent> = mutableListOf()

    var activeInfoLayer: Layer? = null

    val refresh = {
        for (component in components) {
            component.refresh()
        }
        screen.display()
    }

    val screenSize = screen.getBoundableSize()

    val availableItems = ItemViewer(
            Position.of(0, 0),
            screenSize.withColumns(screenSize.columns / 2),
            screen,
            "Found",
            gameRunner,
            items,
            { index -> gameRunner.hero.takeItem(items.removeAt(index)) },
            refresh
    )

    components += availableItems

    val heroInventory = ItemViewer(
            Position.of(0, 0).relativeToRightOf(availableItems.panel),
            screenSize.withRelativeColumns(-screenSize.columns / 2),
            screen,
            "Backpack",
            gameRunner,
            gameRunner.hero.backpack,
            { index -> items.add(gameRunner.hero.dropItem(index)) },
            refresh
    )

    components += heroInventory

    screen.ifActiveOnInput { input ->
        val mouseAction by lazy { input.asMouseAction() }
        if (input.getInputType() == InputType.Escape) {
            returnToScreen.activate()
        } else if (input.isMouseAction() &&
                mouseAction.actionType == MouseActionType.MOUSE_MOVED) {
            activeInfoLayer?.let(screen::removeLayer)
            for (component in components) {
                if (component.panel.containsPosition(mouseAction.position)) {
                    activeInfoLayer = component.onMouseMoved(mouseAction.position)
                }
            }
            activeInfoLayer?.let(screen::pushLayer)
            screen.display()
        }
    }

    return screen
}