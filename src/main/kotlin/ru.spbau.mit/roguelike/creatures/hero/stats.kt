package ru.spbau.mit.roguelike.creatures.hero

import ru.spbau.mit.roguelike.RandomEnumGetter

/**
 * Represents basic hero stats
 */
class BasicStats(
        stats: Map<Type,Int> = emptyMap()
): Iterable<Map.Entry<BasicStats.Type,Int>> {
    override fun iterator(): Iterator<Map.Entry<Type, Int>> =
            stats.iterator()

    private val stats = stats.toMutableMap()

    /**
     * Possible hero stats
     */
    enum class Type {
        STRENGTH,
        AGILITY,
        DAMAGE;

        companion object: RandomEnumGetter<Type>(
                Type::class.java
        )
    }

    /**
     * Gets hero stat value
     * @param type stat type to get
     * @return stat value or 0 if it is not defined
     */
    operator fun get(type: Type) = stats[type] ?: 0

    /**
     * Sets hero stat value
     * @param type stat type to set
     * @param newValue new stat value
     */
    internal operator fun set(type: Type, newValue: Int) {
        stats[type] = newValue
    }

    /**
     * Adds stat to another
     * @param other stats to add
     * @return BasicStats object which stat values are sums of corresponding
     * stat values in both stats
     */
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

    companion object {
        val HEALTH_PER_STRENGTH: Float = 20f
        val DODGE_PARAMETER_PER_AGILITY: Float = 0.01f
    }
}

/**
 * Represents all possible hero stats
 */
class HeroStats(
        val baseMaxHealth: Float,
        val baseDamage: Float
) {
    /**
     * Hero level
     */
    var level = 1
        internal set

    /**
     * Hero experience
     */
    var experience: Int = 0
        internal set

    /**
     * Hero base basic stats
     */
    var basicStats: BasicStats = BasicStats()
        internal set

    /**
     * Unspent stat points which are used to upgrade basic stats
     */
    var unspentStatPoints: Int = 0
        internal set

    companion object {
        val NEXT_LEVEL_EXPERIENCE_BOUND: Int = 100
        val STAT_POINTS_PER_LEVEL: Int = 3
    }
}

/**
 * Represents an entity which can receive experience and upgrade stats
 */
internal interface StatManager {
    fun receiveExperience(received: Int): Boolean
    fun spendStatPoint(statToUpgrade: BasicStats.Type)
    fun recalculateStats()
}