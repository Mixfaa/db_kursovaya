package com.mixfa.marketplace.shared.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.domain.Sort

interface SortConstructor {
    fun makeSort(): Sort
}

data class AssembleableSort(
    val orders: Map<String, Sort.Direction>
) : SortConstructor {
    override fun makeSort(): Sort {
        if (orders.isEmpty()) return Sort.unsorted()

        val mongoOrders = orders
            .map { (prop, direction) -> Sort.Order(direction, prop) }

        return Sort.by(mongoOrders)
    }
}

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class PrecompiledSort(private val sort: Sort) : SortConstructor {
    PRICE_DESCENDING(Sort.by("actualPrice").descending()),
    PRICE_ASCENDING(Sort.by("actualPrice").ascending()),
    ORDER_COUNT_DESCENDING(Sort.by("ordersCount").descending());

    @JsonIgnore
    override fun makeSort(): Sort = this.sort
}