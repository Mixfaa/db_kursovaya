package com.mixfa.marketplace.prop_indexer.service

import com.mixfa.excify.FastException
import com.mixfa.marketplace.marketplace.model.CATEGORY_MONGO_COLLECTION
import com.mixfa.marketplace.marketplace.model.Category
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.service.CategoryService
import com.mixfa.marketplace.marketplace.service.ProductService
import com.mixfa.marketplace.prop_indexer.model.CLUSTER_REF_MONGO_COLLECTION
import com.mixfa.marketplace.prop_indexer.model.IndexClusterRef
import com.mixfa.marketplace.prop_indexer.model.IndexedProperty
import com.mixfa.shared.fieldName
import com.mixfa.shared.findPageable
import com.mixfa.shared.model.CheckedPageable
import com.mixfa.shared.model.MarketplaceEvent
import com.mongodb.DBRef
import org.bson.types.ObjectId
import org.springframework.context.ApplicationListener
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service


@Service
class IndexerService(
    private val mongoTemplate: MongoTemplate,
) : ApplicationListener<MarketplaceEvent> {

    fun findValues(category: Category, pageable: CheckedPageable): Page<IndexedProperty> {
        val cluster = mongoTemplate.findOne(
            Query(Criteria.where(fieldName(IndexClusterRef::categories)).`in`(category.id)),
            IndexClusterRef::class.java,
            CLUSTER_REF_MONGO_COLLECTION
        ) ?: return Page.empty()

        return mongoTemplate.findPageable<IndexedProperty>(
            Query(),
            pageable,
            cluster.clusterName
        )
    }

    fun findValues(category: Category, prop: String): Collection<String> {
        val cluster = mongoTemplate.findOne(
            Query(Criteria.where(fieldName(IndexClusterRef::categories)).`in`(category.id)),
            IndexClusterRef::class.java,
            CLUSTER_REF_MONGO_COLLECTION
        ) ?: return emptyList()

        return mongoTemplate.findOne(
            Query(Criteria.where("_id").`is`(prop)),
            IndexedProperty::class.java,
            cluster.clusterName
        )?.values?.keys ?: emptyList()
    }

    private fun onProductCreated(product: Product) {
        val cluster = mongoTemplate.findOne(
            Query(
                Criteria.where(fieldName(IndexClusterRef::categories))
                    .all(product.allRelatedCategoriesIds.map(::ObjectId))
            ),
            IndexClusterRef::class.java,
            CLUSTER_REF_MONGO_COLLECTION,
        ) ?: throw FastException("Can`t find cluster for product ${product.id} (${product.caption})")

        for ((prop, value) in product.characteristics) {
            mongoTemplate.updateFirst(
                Query(Criteria.where("_id").`is`(prop)),
                Update().inc("${fieldName(IndexedProperty::values)}.$value", 1),
                cluster.clusterName
            )
        }
    }

    private fun onCategoryCreated(category: Category) {
        if (category.parentCategoryId == null) {
            val newClusterRef = IndexClusterRef(listOf(category))
            mongoTemplate.insert(newClusterRef, CLUSTER_REF_MONGO_COLLECTION)

            for (requiredProp in category.requiredProps)
                mongoTemplate.insert(IndexedProperty(requiredProp, emptyMap()), newClusterRef.clusterName)
        } else { // expand cluster
            mongoTemplate.updateMulti(
                Query(
                    Criteria.where("${fieldName(IndexClusterRef::categories)}.\$id")
                        .`in`(category.parentCategoryId!!)
                ),
                Update().addToSet(
                    fieldName(IndexClusterRef::categories),
                    DBRef(CATEGORY_MONGO_COLLECTION, category.id)
                ),
                CLUSTER_REF_MONGO_COLLECTION
            )
        }
    }

    override fun onApplicationEvent(event: MarketplaceEvent) {
        when (event) {
            is ProductService.Event.ProductRegister -> onProductCreated(event.product)
            is CategoryService.Event.CategoryRegister -> onCategoryCreated(event.category)
            else -> {}
        }
    }
}