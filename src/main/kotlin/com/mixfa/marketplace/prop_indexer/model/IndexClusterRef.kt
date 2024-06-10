package com.mixfa.marketplace.prop_indexer.model

import com.mixfa.marketplace.marketplace.model.Category
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

const val CLUSTER_REF_MONGO_COLLECTION = "index_cluster_ref"

@Document(CLUSTER_REF_MONGO_COLLECTION)
data class IndexClusterRef(
    @DBRef(lazy = true) val categories: List<Category>,
    @Id val id: ObjectId = ObjectId(),
    val clusterName: String = "index_cluster_${ObjectId()}",
)