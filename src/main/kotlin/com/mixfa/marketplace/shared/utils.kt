package com.mixfa.marketplace.shared

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import java.net.http.HttpResponse

const val DEFAULT_FIXED_RATE = 15000L

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

fun Int.httpSuccessful(): Boolean = this >= 200 && this < 300

inline fun <reified T> HttpResponse<String>.mapBodyToOrNull(mapper: ObjectMapper): T? =
    runOrNull { mapper.readValue<T>(this.body()) }

inline fun <reified T> HttpResponse<String>.mapBodyTo(mapper: ObjectMapper): T = mapper.readValue<T>(this.body())

@DelicateCoroutinesApi
fun GlobalScope.launchIO(block: suspend CoroutineScope.() -> Unit) =
    this.launch(Dispatchers.IO, CoroutineStart.DEFAULT, block)
