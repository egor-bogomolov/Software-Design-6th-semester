package ru.spbau.mit.roguelike

import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.map.OutsideGameBordersCell
import ru.spbau.mit.roguelike.map.Position

sealed class LoggerMessage(val text: String) {
    abstract override fun toString(): String
}

class SimpleMessage(text: String): LoggerMessage(text) {
    override fun toString() = text
}

class PositionedMessage(
        val position: Position,
        text: String
): LoggerMessage(text) {
    override fun toString() =
            "<$position> $text"
}

object Logger {
    private val messages = mutableListOf<LoggerMessage>()
    private val newMessages = mutableListOf<LoggerMessage>()

    fun log(text: String) {
        newMessages.add(SimpleMessage(text))
    }

    fun log(position: Position, text: String) {
        newMessages.add(PositionedMessage(position, text))
    }

    fun getNew(): List<LoggerMessage> {
        val result = newMessages.toList()
        messages.addAll(newMessages)
        newMessages.clear()
        return result
    }

    fun getNewVisible(visibleMap: GameMap): List<LoggerMessage> {
        val result = newMessages.toList()
        messages.addAll(newMessages)
        newMessages.clear()
        return result.filter {
            when (it) {
                is SimpleMessage     -> true
                is PositionedMessage -> visibleMap[it.position] !== OutsideGameBordersCell
            }
        }
    }
}