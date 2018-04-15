package ru.spbau.mit.roguelike.hero

import ru.spbau.mit.roguelike.creatures.*
import ru.spbau.mit.roguelike.items.Equipment
import ru.spbau.mit.roguelike.items.Item
import java.util.*
import kotlin.math.exp
import kotlin.math.min

class BasicStats(stats: Map<Type,Int> = emptyMap()) {
    private val stats = stats.toMutableMap()

    enum class Type {
        STRENGTH,
        AGILITY,
        DAMAGE
    }

    operator fun get(type: Type) = stats[type] ?: 0

    internal operator fun set(type: Type, newValue: Int) {
        stats[type] = newValue
    }

    operator fun plus(other: BasicStats): BasicStats =
            BasicStats(
                    other.stats.entries.fold(
                            stats,
                            { cur, (type,value) ->
                                cur[type] = cur[type] ?: 0 + value
                                cur
                            }
                    )
            )
}

private interface StatManager {
    fun receiveExperience(received: Int)
    fun spendStatPoint(statToUpgrade: BasicStats.Type)
    fun recalculateStats()
}

class HeroStats(
        val baseMaxHealth: Float,
        val baseDamage: Float
) {
    var level = 1
        internal set

    var experience: Int = 0
        internal set

    var basicStats: BasicStats = BasicStats()
        internal set

    var unspentStatPoints: Int = 0
        internal set

    var evasion: Float = 1f
        internal set

    companion object {
        val NEXT_LEVEL_EXPERIENCE_BOUND: Int = TODO("resource getter")
        val STAT_POINTS_PER_LEVEL: Int = TODO("resource getter")
        val HEALTH_PER_STRENGTH: Float = TODO("resource getter")
        val REGENERATION_PER_STRENGTH: Float = TODO("resource getter")
        val EVASION_PER_AGILITY: Float = TODO("resource getter")
    }
}

private interface InventoryManager: StatManager {
    val backpack: MutableList<Item>
        get() = emptyList<Item>().toMutableList()

    private val internalEquipment: MutableMap<Equipment.Slot,Equipment>
        get() = emptyMap<Equipment.Slot,Equipment>().toMutableMap()

    val equipment: Map<Equipment.Slot,Equipment>
        get() = internalEquipment

    fun equipItem(backpackIndex: Int) {
        val item = backpack[backpackIndex]
        if (item is Equipment) {
            unequipItem(item.slot)
            internalEquipment[item.slot] = item
            recalculateStats()
        }
    }

    fun unequipItem(slot: Equipment.Slot) {
        val item = internalEquipment[slot] ?: return
        backpack.add(item)
        internalEquipment.remove(slot)
        recalculateStats()
    }
}

abstract class Hero(
        name: String,
        baseMaxHealth: Float,
        baseDamage: Float
): Creature(name, baseMaxHealth, baseDamage), InventoryManager, StatManager {
    val stats = HeroStats(
            baseMaxHealth,
            baseDamage
    )

    var totalStats: BasicStats = stats.basicStats
        private set

    override fun receiveExperience(received: Int) {
        if (received <= 0) {
            throw IllegalArgumentException("received experience should be positive")
        }
        val newLevels = (stats.experience + received) / HeroStats.NEXT_LEVEL_EXPERIENCE_BOUND
        stats.experience = (stats.experience + received) % HeroStats.NEXT_LEVEL_EXPERIENCE_BOUND
        stats.level += newLevels
        stats.unspentStatPoints += HeroStats.STAT_POINTS_PER_LEVEL * newLevels
    }

    override fun spendStatPoint(statToUpgrade: BasicStats.Type) {
        if (stats.unspentStatPoints > 0) {
            stats.basicStats[statToUpgrade]++
            stats.unspentStatPoints--
            recalculateStats()
        }
    }

    override fun recalculateStats() {
        totalStats = equipment.values.fold(
                stats.basicStats,
                { stats, item -> stats + item.stats }
        )
        val newMaxHealth = stats.baseMaxHealth +
                HeroStats.HEALTH_PER_STRENGTH * totalStats[BasicStats.Type.STRENGTH]
        health *= newMaxHealth / maxHealth
        maxHealth = newMaxHealth
        damage = stats.baseDamage + totalStats[BasicStats.Type.DAMAGE]
        stats.evasion = exp(-HeroStats.EVASION_PER_AGILITY * totalStats[BasicStats.Type.AGILITY])
    }

    override fun takeDamage(damage: Float): AttackResult {
        val result = when {
            Random().nextFloat() > stats.evasion -> Dodged
            health > damage                      -> Damaged(damage)
            else                                 -> Died
        }
        logger.info { "$name $result" }
        return result
    }

    override fun regenerateHealth() {
        val regeneratedHealth =
                totalStats[BasicStats.Type.STRENGTH] * HeroStats.REGENERATION_PER_STRENGTH
        health = min(maxHealth, health + regeneratedHealth)
        logger.info { "$name regenerated $regeneratedHealth health" }
    }
}