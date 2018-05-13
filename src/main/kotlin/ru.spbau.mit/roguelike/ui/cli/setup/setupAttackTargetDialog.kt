package ru.spbau.mit.roguelike.ui.cli.setup

import org.codetome.zircon.api.Size
import org.codetome.zircon.api.builder.TerminalBuilder
import org.codetome.zircon.api.component.builder.RadioButtonGroupBuilder
import org.codetome.zircon.api.screen.Screen
import ru.spbau.mit.roguelike.creatures.*
import ru.spbau.mit.roguelike.formatEnumValue
import ru.spbau.mit.roguelike.ui.cli.CLIGameUI
import java.util.function.Consumer

/**
 * Sets up a screen for hero to choose attack target in a specific map cell
 * @param direction of attack
 * @param possibleTargets to attack
 * @param actionForwarder function which forwards chosen target to attack processor
 * (@see ru.spbau.mit.roguelike.creatures.CreatureManager.processAttack)
 * @param returnToScreen screen to return to
 * @return constructed screen
 */
internal fun CLIGameUI.setupAttackTargetDialog(
        direction: Direction,
        possibleTargets: Set<Creature>,
        actionForwarder: (CreatureAction) -> Unit,
        returnToScreen: Screen
): Screen {
    val screen = TerminalBuilder.createScreenFor(terminal)

    val panel = panelTemplate
            .title("Choose target")
            .size(screen
                    .getBoundableSize()
                    .minus(Size.of(2, 2))
            )
            .build()

    screen.addComponent(panel)

    val targets = RadioButtonGroupBuilder
            .newBuilder()
            .size(panel.getEffectiveSize())
            .build()

    panel.addComponent(targets)

    val orderedCreatures = possibleTargets.toList()

    for ((index, creature) in orderedCreatures.withIndex()) {
        if (creature is Monster) {
            val text = "${formatEnumValue(creature.modifier.name)} ${creature.name} [${creature.health}/${creature.maxHealth}]"

            targets.addOption(index.toString(), text)
        }
    }

    targets.onSelection(Consumer {
        val attackedCreature = orderedCreatures[it.getKey().toInt()]
        actionForwarder(Attack(direction, attackedCreature))
        returnToScreen.activate()
    })

    return screen
}