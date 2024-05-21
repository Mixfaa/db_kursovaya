package com.mixfa.shared

import com.mixfa.excify.FastException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

const val MAX_PAGE_SIZE = 15

fun Pageable.isNotInBound(): Boolean {
    return !(pageSize >= 0 && pageSize <= MAX_PAGE_SIZE)
}

private val largePageSizeException = FastException("Page size is too big, should be <= $MAX_PAGE_SIZE")

fun Pageable.throwIfNotInBound() {
    if (this.isNotInBound()) throw largePageSizeException
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
