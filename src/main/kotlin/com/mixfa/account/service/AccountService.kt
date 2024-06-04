package com.mixfa.account.service

import com.mixfa.account.model.ACCOUNT_MONGO_COLLECTION
import com.mixfa.account.model.Account
import com.mixfa.account.model.Role
import com.mixfa.`excify-either`.makeMemorizedException
import com.mixfa.excify.FastException
import com.mixfa.marketplace.mail.MailSender
import com.mixfa.shared.*
import com.mixfa.shared.model.CheckedPageable
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.apache.commons.collections4.map.PassiveExpiringMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.security.Principal
import java.util.*
import kotlin.collections.set
import kotlin.random.Random


private typealias Mail = String
private typealias MailCode = String

@Service
@Validated
class AccountService(
    private val accountRepo: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val mailSender: MailSender,
    @Value("\${admin.secret}") private val adminSecret: String,
    private val mongoTemplate: MongoTemplate,
) : UserDetailsService {
    private val mailCodes =
        Collections.synchronizedMap(PassiveExpiringMap<MailCode, Mail>(CODE_EXPIRATION_TIME_IN_MILLI))

    @Scheduled(fixedRate = DEFAULT_FIXED_RATE)
    fun removeExpiredMailCodes() {
        mailCodes.size
    }

    fun findUsers(query: String, pageable: CheckedPageable) =
        accountRepo.findAllByText(query, pageable)

    fun accountByPrincipal(principal: Principal): Optional<Account> = accountRepo.findById(principal.name)

    fun getAuthenticatedAccount(): Optional<Account> =
        accountRepo.findById(authenticatedPrincipal().name)

    fun register(@Valid request: Account.RegisterRequest): Account {
        if (accountRepo.existsByUsername(request.username))
            throw FastException("Username ${request.username} is already in use")

        val requestedEmail = mailCodes.remove(request.mailCode)
            ?: throw FastException("Code ${request.mailCode} not related to any email")

        if (accountRepo.existsByEmail(requestedEmail))
            throw makeMemorizedException("Email is already in use")

        val role: Role = if (!request.adminSecret.isNullOrBlank()) Role.ADMIN
        else runOrNull { Role.valueOf(request.role) } ?: Role.CUSTOMER

        if (role == Role.ADMIN) {
            if (request.adminSecret == null)
                throw makeMemorizedException("Requested admin role but admin secret is null")

            if (request.adminSecret != adminSecret)
                throw makeMemorizedException("Invalid admin secret key")
        }

        return accountRepo.save(
            Account(
                username = request.username,
                firstname = request.firstname,
                lastname = request.lastname,
                email = requestedEmail,
                password = passwordEncoder.encode(request.password),
                role = role
            )
        )
    }

    @PreAuthorize(IS_AUTHENTICATED)
    fun addShippingAddress(@Valid @NotBlank shippingAddress: String) {
        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(authenticatedPrincipal().name)),
            Update().addToSet(fieldName(Account::shippingAddresses), shippingAddress),
            ACCOUNT_MONGO_COLLECTION
        )
    }

    @PreAuthorize(IS_AUTHENTICATED)
    fun removeShippingAddress(@Valid @NotBlank shippingAddress: String){
        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(authenticatedPrincipal().name)),
            Update().pull(fieldName(Account::shippingAddresses), shippingAddress),
            ACCOUNT_MONGO_COLLECTION
        )
    }

    fun sendEmailTo(@Valid @NotBlank email: String) {
        val code = takeWhile(mailCodes::containsKey, ::random6DigitCode)

        mailCodes[code] = email
        mailSender.sendMail(email, "marketplace email verification", code)
    }

    override fun loadUserByUsername(username: String): UserDetails {
        return accountRepo.findByUsername(username).orElseThrow { UsernameNotFoundException(username) }
    }

    companion object {
        private const val CODE_EXPIRATION_TIME_IN_MILLI = 5L * 60L * 1000L

        fun random6DigitCode() = String.format("%06d", Random.Default.nextInt(100000, 999999))
    }
}