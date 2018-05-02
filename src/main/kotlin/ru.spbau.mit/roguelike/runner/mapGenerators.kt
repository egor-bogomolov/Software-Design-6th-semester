package ru.spbau.mit.roguelike.runner

import ru.spbau.mit.roguelike.map.*

interface GameMapGenerator {
    fun generateMap(settings: GameSettings): GameMap
}

object EmptyMapGenerator: GameMapGenerator {
    override fun generateMap(settings: GameSettings): GameMap {
        val cells = Array(settings.mapDimensions.second) { row ->
            Array(settings.mapDimensions.first) { column -> Pair(Pair(column + 1, row + 1), FloorCell(emptySet())) }
        }
                .fold(emptyList<Pair<Position,TerrainCell>>()) { l, r -> l + r }
                .toMap()
                .toMutableMap()
        cells[Pair(1, 1)] = WorldEntrance
        cells[settings.mapDimensions] = WorldExit

        return GameMap(cells)
    }
}