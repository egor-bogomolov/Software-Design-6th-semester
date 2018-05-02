package ru.spbau.mit.roguelike.creatures

import ru.spbau.mit.roguelike.map.GameMap
import kotlin.math.exp
import kotlin.math.ln

abstract class Creature(
        val name: String,
        maxHealth: Float,
        damage: Float
) {
    var health: Float = maxHealth
        internal set
    var maxHealth: Float = maxHealth
        internal set
    var damage: Float = damage
        internal set

    internal var dodgeParameter: Float = 0f

    val dodgeChance: Float
        get() = 1 - exp(-dodgeParameter)

    abstract suspend fun askAction(visibleMap: GameMap): CreatureAction
}

class Monster(
        name: String,
        maxHealth: Float,
        damage: Float,
        val modifier: Modifier = Modifier.ORDINARY
): Creature(
        name,
        if (modifier == Modifier.STRONG) {
            maxHealth * STRONG_HEALTH_COEFFICIENT
        } else {
            maxHealth
        },
        if (modifier == Modifier.STRONG) {
            damage * STRONG_DAMAGE_COEFFICIENT
        } else {
            damage
        }
) {
    init {
        if (modifier == Modifier.FAST) {
            dodgeParameter = FAST_DODGE_PARAMETER
        }
    }

    enum class Modifier {
        ORDINARY,
        FAST,
        STRONG
    }

    override suspend fun askAction(visibleMap: GameMap): CreatureAction {
        return PassTurn // TODO("implement actions")
    }

    companion object {
        val FAST_DODGE_PARAMETER = -ln(0.5f)
        const val STRONG_HEALTH_COEFFICIENT = 2
        const val STRONG_DAMAGE_COEFFICIENT = 2
    }
}