package ru.spbau.mit.roguelike

import ru.spbau.mit.roguelike.map.GameMap
import ru.spbau.mit.roguelike.map.OutsideGameBordersCell
import ru.spbau.mit.roguelike.map.Position

/**
 * Represents abstract logger message
 */
internal sealed class LoggerMessage(val text: String) {
    abstract override fun toString(): String
}

/**
 * Represents a simple message containing a single string of text
 */
internal class SimpleMessage(text: String): LoggerMessage(text) {
    override fun toString() = text
}

/**
 * Represents a message connected to a specific position on the map
 * (allows filtration by visible cells)
 */
internal class PositionedMessage(
        val position: Position,
        text: String
): LoggerMessage(text) {
    override fun toString() =
            "<$position> $text"
}

/**
 * Logger object which supports position filtering and showing of new messages only
 */
internal object Logger {
    private val messages = mutableListOf<LoggerMessage>()
    private val newMessages = mutableListOf<LoggerMessage>()

    /**
     * Adds a simple text message to log
     * @param text message text
     */
    fun log(text: String) {
        newMessages.add(SimpleMessage(text))
    }

    /**
     * Adds a positioned message to log
     * @param position position of logged event
     * @param text message text
     */
    fun log(position: Position, text: String) {
        newMessages.add(PositionedMessage(position, text))
    }

    /**
     * Gets all currently unread messages and removes them from `unread`
     * @return list of unread messages
     */
    fun getNew(): List<LoggerMessage> {
        val result = newMessages.toList()
        messages.addAll(newMessages)
        newMessages.clear()
        return result
    }

    /**
     * Gets all visible unread messages and removes them from `unread`
     * @param visibleMap map determining visible cells
     * @return list of unread messages on visible positions
     */
    fun getNewVisible(visibleMap: GameMap): List<LoggerMessage> =
            getNew().filter {
                when (it) {
                    is SimpleMessage     -> true
                    is PositionedMessage -> visibleMap[it.position] !== OutsideGameBordersCell
                }
            }
}