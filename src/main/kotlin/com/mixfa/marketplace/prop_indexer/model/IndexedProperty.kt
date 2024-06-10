package com.mixfa.marketplace.prop_indexer.model

import com.mixfa.shared.model.WithDto
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient

data class IndexedProperty(
    @Id val prop: String,
    val values: Map<String, Long>
) : WithDto {

    @delegate:Transient
    override val asDto: Dto by lazy { Dto(this) }

    data class Dto(
        val prop: String,
        val values: Collection<String>
    ) {
        constructor(indexedProperty: IndexedProperty) :
                this(indexedProperty.prop, indexedProperty.values.keys)
    }
}