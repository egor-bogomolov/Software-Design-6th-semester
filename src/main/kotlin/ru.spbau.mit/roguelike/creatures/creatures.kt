package ru.spbau.mit.roguelike.creatures

import ru.spbau.mit.roguelike.RandomEnumGetter
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.map.Position
import ru.spbau.mit.roguelike.map.manhattanDistance
import ru.spbau.mit.roguelike.map.plus
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

    abstract suspend fun askAction(
            position: Position,
            visibleMap: GameMap,
            visibleCreatures: Map<Position, Set<Creature>>
    ): CreatureAction
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
        STRONG;

        companion object: RandomEnumGetter<Modifier>(
                Modifier::class.java
        )
    }

    override suspend fun askAction(
            position: Position,
            visibleMap: GameMap,
            visibleCreatures: Map<Position,Set<Creature>>
    ): CreatureAction {
        val (heroPosition, hero) = run {
            for ((cellPosition, cellCreatures) in visibleCreatures) {
                for (creature in cellCreatures) {
                    if (creature is Hero) {
                        return@run cellPosition to creature
                    }
                }
            }
            return Move(Direction.RANDOM)
        }

        for (direction in Direction.values()) {
            val pointedCell = position + direction
            if (heroPosition == pointedCell) {
                return Attack(direction, hero)
            } else if (heroPosition.manhattanDistance(pointedCell) <
                    heroPosition.manhattanDistance(position)) {
                return Move(direction)
            }
        }

        return PassTurn
    }

    companion object {
        val FAST_DODGE_PARAMETER = -ln(0.5f)
        const val STRONG_HEALTH_COEFFICIENT = 2
        const val STRONG_DAMAGE_COEFFICIENT = 2
    }
}