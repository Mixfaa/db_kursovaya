package com.mixfa.marketplace.marketplace.service

import com.mixfa.account.service.AccountService
import com.mixfa.marketplace.marketplace.model.Comment
import com.mixfa.marketplace.marketplace.repository.CommentRepository
import com.mixfa.shared.authenticatedPrincipal
import com.mixfa.shared.model.CheckedPageable
import com.mixfa.shared.model.MarketplaceEvent
import com.mixfa.shared.orThrow
import com.mixfa.shared.throwIfNot
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.util.*


@Service
@Validated
class CommentService(
    private val commentRepo: CommentRepository,
    private val accountService: AccountService,
    private val productService: ProductService,
    private val eventPublisher: ApplicationEventPublisher
) : ApplicationListener<ProductService.Event> {
    @Transactional
    @PreAuthorize("hasAuthority('COMMENTS:EDIT')")
    open fun registerComment(@Valid request: Comment.RegisterRequest): Comment {
        val account = accountService.getAuthenticatedAccount().orThrow()
        val product = productService.findProductById(request.productId).orThrow()

        return commentRepo.save(
            Comment(
                owner = account,
                product = product,
                content = request.content,
                rate = request.rate,
                timestamp = Calendar.getInstance().time
            )
        ).also { comment -> eventPublisher.publishEvent(Event.CommentRegister(comment, this)) }
    }

    @Transactional
    @PreAuthorize("hasAuthority('COMMENTS:EDIT')")
    open fun deleteComment(commentId: String) {
        val comment = commentRepo.findById(commentId).orThrow()
        authenticatedPrincipal().throwIfNot(comment.owner)

        commentRepo.delete(comment)
        eventPublisher.publishEvent(Event.CommentDelete(comment, this))
    }

    fun listProductComments(productId: String, pageable: CheckedPageable) =
        commentRepo.findAllByProductId(ObjectId(productId), pageable)

    @PreAuthorize("hasAuthority('COMMENTS:EDIT')")
    fun findMyComments(page: CheckedPageable) = commentRepo.findAllByOwnerUsername(authenticatedPrincipal().name, page)

    private fun deleteCommentsByProductId(productId: String) = commentRepo.deleteAllByProductId(ObjectId(productId))

    override fun onApplicationEvent(event: ProductService.Event) = when (event) {
        is ProductService.Event.ProductDelete -> deleteCommentsByProductId(event.productId)
        else -> {}
    }

    sealed class Event(src: Any) : MarketplaceEvent(src) {
        class CommentRegister(val comment: Comment, src: Any) : Event(src)
        class CommentDelete(val comment: Comment, src: Any) : Event(src)
    }
}