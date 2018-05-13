package ru.spbau.mit.roguelike.runner

import ru.spbau.mit.roguelike.creatures.Direction
import ru.spbau.mit.roguelike.creatures.hero.BasicStats
import ru.spbau.mit.roguelike.formatEnumValue
import ru.spbau.mit.roguelike.items.Equipment
import ru.spbau.mit.roguelike.items.Item
import ru.spbau.mit.roguelike.items.Junk
import ru.spbau.mit.roguelike.map.*
import java.util.*

/**
 * Interface for map generation
 */
interface GameMapGenerator {
    /**
     * Generates map of dimensions from given settings
     * @param settings to get dimensions from
     * @return generated map
     */
    fun generateMap(settings: GameSettings): GameMap
}

/**
 * Generates an empty map
 */
object EmptyMapGenerator: GameMapGenerator {
    override fun generateMap(settings: GameSettings): GameMap {
        val cells = Array(settings.mapDimensions.second) { row ->
            Array(settings.mapDimensions.first) { column -> Position(column + 1, row + 1) to FloorCell(emptyList()) }
        }
                .fold(emptyList<Pair<Position,TerrainCell>>()) { l, r -> l + r }
                .toMap()
                .toMutableMap()
        cells[Pair(1, 1)] = WorldEntrance
        cells[settings.mapDimensions] = WorldExit

        return GameMap(cells)
    }
}

/**
 * Generates a sample map
 */
object MazeGenerator: GameMapGenerator {
    private val random = Random()

    private fun generateItems(): List<Item> {
        val randomFloat = random.nextFloat()

        val item = when {
            randomFloat < 0.8 -> null
            randomFloat < 0.9 -> Junk("Bone", "an old bone of some unlucky adventurer")
            else              -> {
                val randomSlot = Equipment.Slot.RANDOM
                val randomStat = BasicStats.Type.RANDOM
                Equipment(
                        formatEnumValue(randomSlot.toString()),
                        "sample item",
                        randomSlot,
                        BasicStats(mapOf(randomStat to 1))
                )
            }
        }

        return@generateItems if (item != null) {
            listOf(item)
        } else {
            emptyList()
        }
    }

    override fun generateMap(settings: GameSettings): GameMap {
        val cells = Array(settings.mapDimensions.second) { row ->
            Array(settings.mapDimensions.first) { column -> Position(column + 1, row + 1) to WallCell }
        }
                .fold(emptyList<Pair<Position,TerrainCell>>()) { l, r -> l + r }
                .toMap()
                .toMutableMap()
        val entrancePosition = Position(1, 1)
        cells[entrancePosition] = WorldEntrance
        cells[settings.mapDimensions] = WorldExit

        val visited = Array(settings.mapDimensions.first) { x ->
            Array(settings.mapDimensions.second) { y ->
                Position(x + 1, y + 1) to false
            }
        }.flatten().toMap().toMutableMap()

        fun generatePath(position: Position) {
            visited[position] = true

            val currentCell = cells[position] ?: OutsideGameBordersCell
            when (currentCell) {
                OutsideGameBordersCell -> return@generatePath
                WorldExit              -> return@generatePath
                is WorldEntrance       -> {}
                is PassableCell        -> return@generatePath
                else                   -> {
                    val isDeadEnd = Direction.values()
                            .count {cells[position + it] is PassableCell } == 1
                    val isExitNear = Direction.values()
                            .any {cells[position + it] is WorldExit }
                    when {
                        !isDeadEnd               -> return@generatePath
                        !isExitNear && random.nextFloat() > 0.9 -> {
                            cells[position] = Chest(
                                    List(10) { generateItems() }.flatten()
                            )
                            return@generatePath
                        }
                        else                     -> cells[position] = FloorCell(generateItems())
                    }
                }
            }
            for (direction in Direction
                    .values()
                    .asIterable()
                    .shuffled()) {
                val newPosition = position + direction
                if (visited[newPosition] == false) {
                    generatePath(newPosition)
                }
            }
        }

        generatePath(entrancePosition)

        return GameMap(cells)
    }
}