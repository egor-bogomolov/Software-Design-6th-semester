package ru.spbau.mit.roguelike

import java.net.URL
import java.util.*

internal fun formatEnumValue(enumValue: String): String =
        enumValue.toLowerCase().capitalize()

internal fun String.asResource(): URL {
    val resource = ClassLoader.getSystemResource(this)
    return resource
            ?: throw IllegalArgumentException("resource $this not found")
}

open class RandomEnumGetter<T: Enum<T>>(clazz: Class<T>) {
    private val values = clazz.enumConstants
    private val random = Random()

    val RANDOM: T
        get() = values[random.nextInt(values.size)]
}