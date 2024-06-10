package com.mixfa.marketplace.prop_indexer.model

import org.bson.types.ObjectId

data class ProductCreatedEvent(
    val id: ObjectId,
    val characteristics: Map<String,String>,
    val allRelatedCategoriesIds: Collection<String>
)