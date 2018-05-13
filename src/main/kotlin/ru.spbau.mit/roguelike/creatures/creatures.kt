package ru.spbau.mit.roguelike.creatures

import ru.spbau.mit.roguelike.RandomEnumGetter
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.map.Position
import ru.spbau.mit.roguelike.map.manhattanDistance
import ru.spbau.mit.roguelike.map.plus
import kotlin.math.exp
import kotlin.math.ln

/**
 * Base class for all creatures, including Hero
 */
abstract class Creature(
        val name: String,
        maxHealth: Float,
        damage: Float
) {
    /**
     * Creature current health
     */
    var health: Float = maxHealth
        internal set

    /**
     * Creature maximum health
     */
    var maxHealth: Float = maxHealth
        internal set

    /**
     * Current creature damage
     */
    var damage: Float = damage
        internal set

    /**
     * Creature's internal dodge parameter
     */
    internal var dodgeParameter: Float = 0f

    /**
     * Dodge chance defined by @see dodgeParameter in the following way:
     * dodgeChance = 1 - e ^ (-dodgeParameter)
     */
    val dodgeChance: Float
        get() = 1 - exp(-dodgeParameter)

    /**
     * Calculates next creature action based on its position,
     * visible map and visible creatures on the max
     * @param position current creature position
     * @param visibleMap visible map cells
     * @param visibleCreatures visible creatures on the map
     * @return CreatureAction it would like to perform on its current turn
     */
    abstract suspend fun askAction(
            position: Position,
            visibleMap: GameMap,
            visibleCreatures: Map<Position, Set<Creature>>
    ): CreatureAction
}

/**
 * Represents monster-like creature
 */
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

    /**
     * Modified special for monsters which changes some of their stats:
     * - a fast monster has 50% dodge chance, which an ordinary monster does not possess
     * - a strong monster has 2x the HP and hits 2 times harder than ordinary one
     */
    enum class Modifier {
        ORDINARY,
        FAST,
        STRONG;

        companion object: RandomEnumGetter<Modifier>(
                Modifier::class.java
        )
    }

    /**
     * A simple monster logic function:
     * - if it sees no Hero on the map, he moves in a random direction
     * - otherwise:
     *   - if hero is seen, but is not in a neighbour cell the monster moves
     *     in a way trying to reduce manhattan distance to hero
     *   - if hero is in a neighbour cell monster tries to attack him
     */
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

        // this one should be impossible to reach but just in case ;)
        return PassTurn
    }

    companion object {
        val FAST_DODGE_PARAMETER = -ln(0.5f)
        const val STRONG_HEALTH_COEFFICIENT = 2
        const val STRONG_DAMAGE_COEFFICIENT = 2
    }
}