package com.mixfa.marketplace.shared

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