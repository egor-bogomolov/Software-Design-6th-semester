package ru.spbau.mit.roguelike

import java.util.*

/**
 * Formats enumeration string value to a readable form
 * @param enumValue stringified enumeration value (usually all caps)
 * @return enumValue converted to capitalized lowercase form
 */
internal fun formatEnumValue(enumValue: String): String =
        enumValue.toLowerCase().capitalize()

/**
 * Gets random value from given enumeration class
 * (main intention is to provide RANDOM option in enumeration companion object)
 */
open class RandomEnumGetter<T: Enum<T>>(clazz: Class<T>) {
    private val values = clazz.enumConstants
    private val random = Random()

    val RANDOM: T
        get() = values[random.nextInt(values.size)]
}