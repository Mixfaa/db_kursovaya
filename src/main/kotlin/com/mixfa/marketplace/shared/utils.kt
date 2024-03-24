package com.mixfa.marketplace.shared

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

