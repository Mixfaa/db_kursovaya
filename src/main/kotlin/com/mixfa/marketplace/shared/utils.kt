package com.mixfa.marketplace.shared

import org.slf4j.Logger

const val DEFAULT_FIXED_RATE = 15000L

inline fun <T> runCatchLog(logger: Logger, block: () -> T): T? {
    try {
        return block()
    } catch (ex: Exception) {
        logger.error(ex.localizedMessage ?: "unknown error ($ex)")
    }
    return null
}

inline fun <R> runOrNull(block: () -> R): R? {
    return try {
        block()
    } catch (ex: Exception) {
        null
    }
}

inline fun <T> Iterable<T>.contains(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

inline fun <T> takeWhile(predicate: (T) -> Boolean, supplier: () -> T): T {
    var value: T
    do {
        value = supplier()
    } while (predicate(value))
    return value
}

inline fun <T> takeUntil(predicate: (T) -> Boolean, supplier: () -> T): T {
    var value: T
    do {
        value = supplier()
    } while (!predicate(value))
    return value
}

