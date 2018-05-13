package ru.spbau.mit.roguelike.runner

import ru.spbau.mit.roguelike.map.*

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
            Array(settings.mapDimensions.first) { column -> Pair(Pair(column + 1, row + 1), FloorCell(emptyList())) }
        }
                .fold(emptyList<Pair<Position,TerrainCell>>()) { l, r -> l + r }
                .toMap()
                .toMutableMap()
        cells[Pair(1, 1)] = WorldEntrance
        cells[settings.mapDimensions] = WorldExit

        return GameMap(cells)
    }
}

// TODO("add normal map generator")