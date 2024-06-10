package com.mixfa.marketplace.prop_indexer.model

import com.mixfa.marketplace.marketplace.model.Category
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

const val INDEX_COLLECTION_MONGO_COLLECTION = "index_collection_ref"

@Document(INDEX_COLLECTION_MONGO_COLLECTION)
data class IndexCollectionRef(
    @DBRef(lazy = true) val categories: List<Category>,
    @Id val id: ObjectId = ObjectId(),
    val collectionName: String = "index_collection_${ObjectId()}",
)