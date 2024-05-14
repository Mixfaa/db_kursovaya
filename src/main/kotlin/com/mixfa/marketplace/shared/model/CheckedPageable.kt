package com.mixfa.marketplace.shared.model

import com.mixfa.marketplace.shared.throwIfNotInBound
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

@JvmInline
value class CheckedPageable(
    private val pageable: Pageable
) : Pageable by pageable {
    init { pageable.throwIfNotInBound() }
    constructor(page: Int, pageSize: Int, sort: Sort) : this(PageRequest.of(page, pageSize, sort))
    constructor(page: Int, pageSize: Int) : this(PageRequest.of(page, pageSize))
    override fun next(): CheckedPageable = CheckedPageable(pageable.next())
    override fun previousOrFirst(): CheckedPageable = CheckedPageable(pageable.previousOrFirst())
    override fun first(): CheckedPageable = CheckedPageable(pageable.first())
    override fun withPage(pageNumber: Int): CheckedPageable = CheckedPageable(pageable.withPage(pageNumber))
}