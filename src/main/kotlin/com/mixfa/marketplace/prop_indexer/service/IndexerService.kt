package com.mixfa.marketplace.prop_indexer.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mixfa.marketplace.marketplace.model.Category
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.service.CategoryService
import com.mixfa.marketplace.marketplace.service.ProductService
import com.mixfa.marketplace.prop_indexer.model.CategoryCreatedEvent
import com.mixfa.marketplace.prop_indexer.model.ProductCreatedEvent
import com.mixfa.shared.model.MarketplaceEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service

@Service
class IndexerService(
    private val rabbitTemplate: RabbitTemplate,
    private val mapper: ObjectMapper
) : ApplicationListener<MarketplaceEvent> {

    private fun onProductCreated(product: Product) {
        rabbitTemplate.convertAndSend(
            "indexer-product-created",
            ProductCreatedEvent(
                product.id,
                product.characteristics,
                product.allRelatedCategoriesIds
            ).let(mapper::writeValueAsBytes)
        )
    }

    private fun onCategoryCreated(category: Category) {
        rabbitTemplate.convertAndSend(
            "indexer-category-created",
            CategoryCreatedEvent(
                category.id,
                category.parentCategoryId,
                category.requiredProps
            ).let(mapper::writeValueAsBytes)
        )
    }

    override fun onApplicationEvent(event: MarketplaceEvent) {
        when (event) {
            is ProductService.Event.ProductRegister -> onProductCreated(event.product)
            is CategoryService.Event.CategoryRegister -> onCategoryCreated(event.category)
            else -> {}
        }
    }
}