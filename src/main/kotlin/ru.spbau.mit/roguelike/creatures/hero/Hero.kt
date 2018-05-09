package ru.spbau.mit.roguelike.creatures.hero

import ru.spbau.mit.roguelike.creatures.Creature
import ru.spbau.mit.roguelike.items.Equipment
import ru.spbau.mit.roguelike.items.Item

private interface InventoryManager: StatManager {
    val backpack: List<Item>

    val equipment: Map<Equipment.Slot,Equipment>

    fun takeItem(item: Item)
    fun dropItem(backpackIndex: Int): Item
    fun equipItem(backpackIndex: Int)
    fun unequipItem(slot: Equipment.Slot)
}

abstract class Hero(
        name: String
): Creature(name, 100f, 10f), InventoryManager, StatManager { // TODO("resource getter")
    val stats = HeroStats(
            maxHealth,
            damage
    )

    override val backpack: List<Item>
        get() = internalBackpack

    override val equipment: Map<Equipment.Slot,Equipment>
        get() = internalEquipment

    var totalStats: BasicStats = stats.basicStats
        private set

    private val internalBackpack: MutableList<Item> =
            mutableListOf()

    private val internalEquipment: MutableMap<Equipment.Slot,Equipment> =
            mutableMapOf()

    override fun takeItem(item: Item) {
        internalBackpack.add(item)
    }

    override fun dropItem(backpackIndex: Int): Item =
            internalBackpack.removeAt(backpackIndex)

    override fun equipItem(backpackIndex: Int) {
        val item = internalBackpack[backpackIndex]
        if (item is Equipment) {
            unequipItem(item.slot)
            internalEquipment[item.slot] = item
            internalBackpack.removeAt(backpackIndex)
            recalculateStats()
        }
    }

    override fun unequipItem(slot: Equipment.Slot) {
        val item = internalEquipment[slot] ?: return
        internalBackpack.add(item)
        internalEquipment.remove(slot)
        recalculateStats()
    }

    override fun receiveExperience(received: Int): Boolean {
        if (received <= 0) {
            throw IllegalArgumentException("received experience should be positive")
        }
        val newLevels = (stats.experience + received) / HeroStats.NEXT_LEVEL_EXPERIENCE_BOUND
        stats.experience = (stats.experience + received) % HeroStats.NEXT_LEVEL_EXPERIENCE_BOUND
        stats.level += newLevels
        stats.unspentStatPoints += HeroStats.STAT_POINTS_PER_LEVEL * newLevels
        return newLevels > 0
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
                BasicStats.HEALTH_PER_STRENGTH * totalStats[BasicStats.Type.STRENGTH]
        health *= newMaxHealth / maxHealth
        maxHealth = newMaxHealth
        damage = stats.baseDamage + totalStats[BasicStats.Type.DAMAGE]
        dodgeParameter = BasicStats.DODGE_PARAMETER_PER_AGILITY * totalStats[BasicStats.Type.AGILITY]
    }

    abstract fun exchangeItems(items: MutableList<Item>)
}