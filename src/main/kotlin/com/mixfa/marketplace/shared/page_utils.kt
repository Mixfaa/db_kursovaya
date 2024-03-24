package com.mixfa.marketplace.shared

import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.account.model.matchesById
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.security.Principal

const val MAX_PAGE_SIZE = 15
val pageableRange = 1..MAX_PAGE_SIZE

fun Pageable.isNotInBound(): Boolean {
    return this.pageSize !in pageableRange
}

fun Pageable.throwIfNotInBound() {
    if (this.isNotInBound()) throw LargePageSizeException.get()
}

fun Principal.throwIfNot(account: Account) {
    if (!this.matchesById(account)) throw FastAccessException.get()
}

inline fun <T> iteratePages(fetchMethod: (Pageable) -> Page<T>, handler: (T) -> Unit) {
    var page: Page<T>
    var pageable = PageRequest.of(0, MAX_PAGE_SIZE)
    do {
        page = fetchMethod(pageable)

        page.forEach(handler)

        pageable = pageable.next()
    } while (page.hasNext())
}

inline fun <T, Arg1> bindToFetchFun(crossinline func: (Arg1, Pageable) -> Page<T>, arg1: Arg1): (Pageable) -> Page<T> {
    return { func(arg1, it) }
}

inline fun <T, Arg1, Arg2> bindToFetchFun(
    crossinline func: (Arg1, Arg2, Pageable) -> Page<T>,
    arg1: Arg1,
    arg2: Arg2
): (Pageable) -> Page<T> {
    return { func(arg1, arg2, it) }
}

inline fun <T, Arg1, Arg2, Arg3> bindToFetchFun(
    crossinline func: (Arg1, Arg2, Arg3, Pageable) -> Page<T>,
    arg1: Arg1,
    arg2: Arg2,
    arg3: Arg3
): (Pageable) -> Page<T> {
    return { func(arg1, arg2, arg3, it) }
}

inline fun <T, Arg1, Arg2, Arg3, Arg4> bindToFetchFun(
    crossinline func: (Arg1, Arg2, Arg3, Arg4, Pageable) -> Page<T>,
    arg1: Arg1,
    arg2: Arg2,
    arg3: Arg3,
    arg4: Arg4
): (Pageable) -> Page<T> {
    return { func(arg1, arg2, arg3, arg4, it) }
}
