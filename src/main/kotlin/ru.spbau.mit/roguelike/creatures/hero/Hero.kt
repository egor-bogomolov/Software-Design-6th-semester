package ru.spbau.mit.roguelike.creatures.hero

import ru.spbau.mit.roguelike.creatures.Creature
import ru.spbau.mit.roguelike.items.Equipment
import ru.spbau.mit.roguelike.items.Item

/**
 * Represents an entity which can manage items in inventory
 * as well as equipped items
 */
private interface InventoryManager: StatManager {
    /**
     * List of unequipped items
     */
    val backpack: List<Item>

    /**
     * Equipped items (no more than one in a slot)
     */
    val equipment: Map<Equipment.Slot,Equipment>

    /**
     * Takes item to inventory
     * @param item to take
     */
    fun takeItem(item: Item)

    /**
     * Drops item from inventory
     * @param backpackIndex of item to drop
     */
    fun dropItem(backpackIndex: Int): Item

    /**
     * Equips item from inventory
     * @param backpackIndex of item to equip
     */
    fun equipItem(backpackIndex: Int)

    /**
     * Unequips item and places it to inventory
     * @param slot to empty
     */
    fun unequipItem(slot: Equipment.Slot)
}

/**
 * Abstract hero which should have its
 * askAction and exchangeItems methods implemented for each GameUI implementation
 */
abstract class Hero(
        name: String
): Creature(name, 100f, 10f), InventoryManager, StatManager { // TODO("resource getter")
    /**
     * Hero stats instance
     */
    val stats = HeroStats(
            maxHealth,
            damage
    )

    /**
     * Hero inventory
     */
    override val backpack: List<Item>
        get() = internalBackpack

    /**
     * Hero equipped items
     */
    override val equipment: Map<Equipment.Slot,Equipment>
        get() = internalEquipment

    /**
     * Hero total stats after adding stats of equipped items
     */
    var totalStats: BasicStats = stats.basicStats
        private set

    private val internalBackpack: MutableList<Item> =
            mutableListOf()

    private val internalEquipment: MutableMap<Equipment.Slot,Equipment> =
            mutableMapOf()

    /**
     * @inheritDoc
     */
    override fun takeItem(item: Item) {
        internalBackpack.add(item)
    }

    /**
     * @inheritDoc
     */
    override fun dropItem(backpackIndex: Int): Item =
            internalBackpack.removeAt(backpackIndex)

    /**
     * @inheritDoc
     */
    override fun equipItem(backpackIndex: Int) {
        val item = internalBackpack[backpackIndex]
        if (item is Equipment) {
            unequipItem(item.slot)
            internalEquipment[item.slot] = item
            internalBackpack.removeAt(backpackIndex)
            recalculateStats()
        }
    }

    /**
     * @inheritDoc
     */
    override fun unequipItem(slot: Equipment.Slot) {
        val item = internalEquipment[slot] ?: return
        internalBackpack.add(item)
        internalEquipment.remove(slot)
        recalculateStats()
    }

    /**
     * @inheritDoc
     */
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

    /**
     * @inheritDoc
     */
    override fun spendStatPoint(statToUpgrade: BasicStats.Type) {
        if (stats.unspentStatPoints > 0) {
            stats.basicStats[statToUpgrade]++
            stats.unspentStatPoints--
            recalculateStats()
        }
    }

    /**
     * @inheritDoc
     */
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

    /**
     * Exchanges items with hero inventory based on player's selection
     * @param items external item list to exchange with
     */
    abstract fun exchangeItems(items: MutableList<Item>)
}