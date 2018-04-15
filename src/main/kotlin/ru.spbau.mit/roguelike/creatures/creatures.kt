package ru.spbau.mit.roguelike.creatures

import mu.KLogging
import ru.spbau.mit.roguelike.map.GameMap
import java.lang.Float.min
import java.util.*

sealed class AttackResult(
        private val stringRepresentation: String
) {
    override fun toString(): String = stringRepresentation
}

class Damaged(
        actualDamage: Float
): AttackResult("suffered $actualDamage damage")

object Dodged: AttackResult("dodged")

object Died: AttackResult("died")

object NoTarget: AttackResult("")

abstract class Creature(
        val name: String,
        maxHealth: Float,
        damage: Float
) {
    var health: Float = maxHealth
        protected set
    var maxHealth: Float = maxHealth
        protected set
    var damage: Float = damage
        protected set

    abstract fun takeDamage(damage: Float): AttackResult
    abstract fun regenerateHealth()
    abstract fun askAction(visibleMap: GameMap): CreatureAction

    protected companion object: KLogging()
}

class Monster private constructor(
        name: String,
        maxHealth: Float,
        damage: Float
): Creature(name, maxHealth, damage) {

    enum class Modifier(private val stringRepresentation: String) {
        ORDINARY("ordinary"),
        FAST("fast"),
        STRONG("strong");

        override fun toString(): String = stringRepresentation
    }

    var modifier = Modifier.ORDINARY
        set(newValue) {
            if (newValue == Modifier.STRONG &&
                    modifier != Modifier.STRONG) {
                maxHealth *= 2
                damage *= 2
                health *= 2
            } else if (newValue != Modifier.STRONG &&
                    modifier == Modifier.STRONG) {
                maxHealth /= 2
                damage /= 2
                health /= 2
            }
        }

    override fun takeDamage(damage: Float): AttackResult {
        val result = if (modifier == Modifier.FAST && Random().nextBoolean()) {
            Dodged
        } else {
            Damaged(damage)
        }
        logger.info { "$name $result" }
        if (damage >= health) {
            logger.info { "$name $Died" }
            return Died
        }
        return result
    }

    override fun regenerateHealth() {
        val regeneratedHealth = 0.5f // TODO("get resource")
        health = min(maxHealth, health + regeneratedHealth)
        logger.info { "$name regenerated $regeneratedHealth health" }
    }

    override fun askAction(visibleMap: GameMap): CreatureAction {
        return PassTurn // TODO("implement actions")
    }
}