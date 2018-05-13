package ru.spbau.mit.roguelike.creatures

import ru.spbau.mit.roguelike.Logger
import ru.spbau.mit.roguelike.creatures.hero.Hero
import ru.spbau.mit.roguelike.map.*
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Creature manager, which handles some of creature actions and their existence in the game
 */
class CreatureManager(
        val hero: Hero,
        otherCreatures: Map<Position,Set<Creature>>,
        private val gameMap: GameMap
) {
    private val internalCreatures: MutableMap<Position,MutableSet<Creature>>

    /**
     * Structure used to determine creatures in a specific cell
     */
    val creatures: Map<Position,Set<Creature>>
        get() = internalCreatures

    /**
     * Hero position
     */
    var heroPosition: Position
        private set

    /**
     * Is hero still in game
     */
    var heroAlive: Boolean = true
        private set

    init {
        if (otherCreatures.filterNot {
                    gameMap[it.key] is PassableCell
                }.isNotEmpty()) {
            throw IllegalStateException("spawned a creature in impassable cell")
        }
        internalCreatures = otherCreatures
                .mapValues {
                    it.value.toMutableSet()
                }
                .toMutableMap()
        internalCreatures
                .putIfAbsent(gameMap.entrance, mutableSetOf())
        internalCreatures[gameMap.entrance]!!.add(hero)
        heroPosition = gameMap.entrance
    }

    /**
     * Convenience [] operator getting creatures from a specific cell
     * @param position to get
     * @return creature in this Position
     */
    operator fun get(position: Position): Set<Creature> =
            internalCreatures[position] ?: emptySet()

    /**
     * Processes Move creature action
     * @param creature moving creature
     * @param position creature position (so we don't have to search for it)
     * @param direction move direction
     */
    fun processMove(
            creature: Creature,
            position: Position,
            direction: Direction
    ) {
        val newPosition = position + direction

        if (gameMap[position] === OutsideGameBordersCell ||
                gameMap[newPosition] is ImpassableCell ||
                creature !in this[position]) {
            return
        }

        internalCreatures[position]!!.remove(creature)
        internalCreatures.getOrPut(newPosition, { mutableSetOf() }).add(creature)
        if (creature is Hero) {
            heroPosition += direction
            val cell = gameMap[heroPosition]
            if (cell is PassableCell && cell.lyingItems.isNotEmpty()) {
                Logger.log("there are some items on the ground")
            }
        }
    }

    /**
     * Processes Attack creature action
     * @param attacker attacking creature
     * @param position position of the attacker
     * @param direction direction of the attack
     * @param attacked attacked creature
     * @return boolean value representing whether the attacked creature has died or not
     */
    fun processAttack(
            attacker: Creature,
            position: Position,
            direction: Direction,
            attacked: Creature
    ): Boolean {
        val attackedPosition = position + direction

        if (attacker !in this[position] ||
                attacked !in this[attackedPosition]) {
            return false
        }

        val logPrefix = "${attacker.name} attacked"

        val logBody = attacked.name +
                " " +
                if (Random().nextFloat() > attacked.dodgeChance) {
                    attacked.health -= attacker.damage
                    "suffered ${attacker.damage} damage"
                } else {
                    "dodged"
                }

        Logger.log(position, "$logPrefix: $logBody")

        return if (attacked.health <= 0) {
            Logger.log(attackedPosition, "${attacked.name} died")
            internalCreatures[attackedPosition]!!.remove(attacked)
            if (attacked is Hero) {
                heroAlive = false
            } else if (attacker is Hero) {
                val levelledUp = attacker.receiveExperience(
                        ceil(attacked.maxHealth / attacker.stats.level).roundToInt()
                )
                if (levelledUp) {
                    Logger.log("${attacker.name} levelled up (healed fully)")
                }
            }
            true
        } else {
            false
        }
    }
}