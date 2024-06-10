package com.mixfa.marketplace.prop_indexer.model

import org.bson.types.ObjectId

data class CategoryCreatedEvent(
    val id: ObjectId,
    val parentCategoryId: ObjectId?,
    val requiredProps: Collection<String>
)