package ru.spbau.mit.roguelike.creatures.hero

class BasicStats(
        stats: Map<Type,Int> = emptyMap()
): Iterable<Map.Entry<BasicStats.Type,Int>> {
    override fun iterator(): Iterator<Map.Entry<Type, Int>> =
            stats.iterator()

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

    companion object {
        val HEALTH_PER_STRENGTH: Float = 20f
        val DODGE_PARAMETER_PER_AGILITY: Float = 0.01f
    }
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

    companion object {
        val NEXT_LEVEL_EXPERIENCE_BOUND: Int = 100
        val STAT_POINTS_PER_LEVEL: Int = 3
    }
}

internal interface StatManager {
    fun receiveExperience(received: Int): Boolean
    fun spendStatPoint(statToUpgrade: BasicStats.Type)
    fun recalculateStats()
}