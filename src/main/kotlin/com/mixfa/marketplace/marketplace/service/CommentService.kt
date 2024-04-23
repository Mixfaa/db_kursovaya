package com.mixfa.marketplace.marketplace.service

import com.mixfa.marketplace.account.service.AccountService
import com.mixfa.marketplace.marketplace.model.Comment
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.service.repo.CommentRepository
import com.mixfa.marketplace.shared.*
import com.mixfa.marketplace.shared.event.MarketplaceEvent
import com.mixfa.marketplace.shared.model.CheckedPageable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepo: CommentRepository,
    private val accountService: AccountService,
    private val productService: ProductService,
    private val eventPublisher: ApplicationEventPublisher
) : ApplicationListener<ProductService.Event> {
    @Transactional
    @PreAuthorize("hasAuthority('COMMENTS:EDIT')")
    open fun registerComment(request: Comment.RegisterRequest): Comment {
        val account = accountService.getAuthenticatedAccount().orThrow()
        val product = productService.findProductById(request.productId).orThrow()

        return commentRepo.save(
            Comment(
                owner = account, product = product, content = request.content, rate = request.rate
            )
        ).also { comment -> eventPublisher.publishEvent(Event.CommentRegister(comment, this)) }
    }

    @PreAuthorize("hasAuthority('COMMENTS:EDIT')")
    fun deleteComment(commentId: String) {
        val principal = SecurityUtils.getAuthenticatedPrincipal()
        val comment = commentRepo.findById(commentId).orThrow()
        principal.throwIfNot(comment.owner)

        commentRepo.delete(comment)
        eventPublisher.publishEvent(Event.CommentDelete(comment, this))
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteCommentsByProductId(product: Product) = commentRepo.deleteAllByProduct(product)

    fun listProductComments(productId: String, pageable: CheckedPageable) =
        commentRepo.findAllByProductId(productId, pageable)

    override fun onApplicationEvent(event: ProductService.Event) = when (event) {
        is ProductService.Event.ProductDelete -> deleteCommentsByProductId(event.product)
        is ProductService.Event.ProductRegister -> {}
    }

    sealed class Event(src: Any) : MarketplaceEvent(src) {
        class CommentRegister(val comment: Comment, src: Any) : Event(src)
        class CommentDelete(val comment: Comment, src: Any) : Event(src)
    }
}