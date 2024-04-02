package com.mixfa.marketplace.shared.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.domain.Sort

data class SortConstructor(
    val orders: Map<String, Sort.Direction>
) {
    fun makeSort(): Sort {
        if (orders.isEmpty()) return Sort.unsorted()

        val mongoOrders =
            orders.map { (prop, direction) -> Sort.Order(direction, prop) }

        return Sort.by(mongoOrders)
    }
}

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class PrecompiledSort(val sort: Sort) {
    PRICE_DESCENDING(Sort.by("price").descending()),
    PRICE_ASCENDING(Sort.by("price").ascending()),
    ORDER_COUNT_DESCENDING(Sort.by("ordersCount").descending());
}