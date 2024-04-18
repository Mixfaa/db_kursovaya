package com.mixfa.marketplace.account.service

import com.mixfa.excify.FastThrowable
import com.mixfa.marketplace.account.AdminSecretIsNullException
import com.mixfa.marketplace.account.UsernameNotValidException
import com.mixfa.marketplace.account.get
import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.account.model.Role
import com.mixfa.marketplace.mail.MailSender
import com.mixfa.marketplace.shared.DEFAULT_FIXED_RATE
import com.mixfa.marketplace.shared.model.CheckedPageable
import com.mixfa.marketplace.shared.orThrow
import com.mixfa.marketplace.shared.runOrNull
import com.mixfa.marketplace.shared.takeWhile
import org.apache.commons.collections4.map.PassiveExpiringMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.Principal
import java.util.*
import kotlin.collections.minus
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.random.Random

@Service
class AccountService(
    private val accountRepo: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val mailSender: MailSender,
    @Value("\${admin.secret}") private val adminSecret: String,
) : UserDetailsService {
    private val mailCodes =
        Collections.synchronizedMap(PassiveExpiringMap<String, String>(CODE_EXPIRATION_TIME_IN_MILLI))

    @Scheduled(fixedRate = DEFAULT_FIXED_RATE)
    fun removeExpiredMailCodes() {
        mailCodes.size
    }

    fun findUsers(query: String, pageable: CheckedPageable) =
        accountRepo.findAllByText(query, pageable)

    fun accountByPrincipal(principal: Principal) = accountRepo.findById(principal.name)

    @PreAuthorize("isAuthenticated() == true")
    fun getAuthenticatedPrincipal(): Authentication =
        SecurityContextHolder.getContext().authentication!!

    @PreAuthorize("isAuthenticated() == true")
    fun getAuthenticatedAccount(): Optional<Account> =
        accountRepo.findById(getAuthenticatedPrincipal().name)

    fun findAccount(accountId: String): Optional<Account> = accountRepo.findById(accountId)

    fun register(request: Account.RegisterRequest): Account {
        throwIfUsernameNotValid(request.username)

        val existsByUsername = accountRepo.existsByUsername(request.username)
        if (existsByUsername) throw FastThrowable("Username ${request.username} is already in use")

        val requestedEmail = mailCodes[request.mailCode]
            ?: throw FastThrowable("Code ${request.mailCode} not related to any email")

        mailCodes.remove(request.mailCode)

        val existsByEmail = accountRepo.existsByEmail(requestedEmail)
        if (existsByEmail) throw FastThrowable("Email $requestedEmail is already in use")

        val role = runOrNull { Role.valueOf(request.role) } ?: Role.CUSTOMER
        if (role == Role.ADMIN) {
            if (request.adminSecret == null)
                throw AdminSecretIsNullException.get()

            if (request.adminSecret != adminSecret)
                throw FastThrowable("Can`t create admin using ${request.adminSecret}")
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

    @PreAuthorize("isAuthenticated() == true")
    fun addShippingAddress(shippingAddress: String): Account {
        val account = getAuthenticatedAccount().orThrow()

        if (account.shippingAddresses.contains(shippingAddress))
            throw FastThrowable("Account already contain $shippingAddress")

        return accountRepo.save(
            account.copy(shippingAddresses = account.shippingAddresses + shippingAddress)
        )
    }

    @PreAuthorize("isAuthenticated() == true")
    fun removeShippingAddress(shippingAddress: String): Account {
        val account = getAuthenticatedAccount().orThrow()

        if (!account.shippingAddresses.contains(shippingAddress))
            throw FastThrowable("Account does not contain $shippingAddress")

        return accountRepo.save(
            account.copy(shippingAddresses = account.shippingAddresses - shippingAddress)
        )
    }

    fun sendEmailTo(email: String) {
        if (email.isBlank()) throw FastThrowable("Can`t send email code to $email")

        val code = takeWhile(mailCodes::containsKey, ::random6DigitCode)

        mailCodes[code] = email
        mailSender.sendSimpleEmail(email, "marketplace email verification", code)
    }

    override fun loadUserByUsername(username: String): UserDetails {
        if (username == GUEST_USERNAME) return guestAccount
        return accountRepo.findByUsername(username).orElseThrow { UsernameNotFoundException(username) }
    }

    companion object {
        private const val CODE_EXPIRATION_TIME_IN_MILLI = 5L * 60L * 1000L
        private const val GUEST_USERNAME = "guest"

        fun random6DigitCode() = String.format("%06d", Random.Default.nextInt(999999))

        private val guestAccount = Account(GUEST_USERNAME, "", "", "", "guest", Role.GUEST)

        private fun throwIfUsernameNotValid(username: String) {
            if (username.isBlank() || username == GUEST_USERNAME) throw UsernameNotValidException.get()
        }
    }
}