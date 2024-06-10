package com.mixfa.shared

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mixfa.shared.model.WithDto
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import java.net.http.HttpResponse
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.ceil
import kotlin.reflect.KProperty

const val DEFAULT_FIXED_RATE = 15000L

fun <T> defaultLazy(block: () -> T) = lazy(LazyThreadSafetyMode.PUBLICATION, block)

fun fieldName(field: KProperty<*>): String = field.name

fun UpdateResult.nothingModified() = this.modifiedCount == 0L

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

@OptIn(ExperimentalEncodingApi::class)
inline fun <reified T> ObjectMapper.readEncoded64(encoded: String): T {
    return this.readValue<T>(Base64.decode(encoded.toByteArray()))
}

inline fun <reified T> MongoTemplate.findIterating(query: Query, collectionName: String, handler: (List<T>) -> Unit) {
    val total = this.count(query, T::class.java, collectionName)

    if (total <= MAX_PAGE_SIZE) {
        this.find(query, T::class.java, collectionName).let(handler)
        return
    }

    val totalPages = ceil(total.toDouble() / MAX_PAGE_SIZE).toLong()

    for (page in 0..totalPages) {
        this.find(query.skip(page * Long.MAX_VALUE).limit(MAX_PAGE_SIZE), T::class.java, collectionName).let(handler)
    }
}

inline fun <reified T> MongoTemplate.findPageable(q: Query, pageable: Pageable, collection: String): Page<T> {
    val total = this.count(q, collection)
    val elements = this.find(q.with(pageable), T::class.java, collection)

    return PageImpl(elements, pageable, total)
}

inline fun <reified T> MongoTemplate.findIterating(query: Query, collectionName: String): List<T> {
    val total = this.count(query, T::class.java, collectionName)

    if (total <= MAX_PAGE_SIZE)
        return this.find(query, T::class.java, collectionName)

    val totalPages = ceil(total.toDouble() / MAX_PAGE_SIZE).toLong()

    return buildList {
        for (page in 0..totalPages) {
            this@findIterating.find(
                query.skip(page * Long.MAX_VALUE).limit(MAX_PAGE_SIZE),
                T::class.java,
                collectionName
            ).let(this::addAll)
        }
    }
}
