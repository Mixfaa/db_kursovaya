package com.mixfa.marketplace.shared

import com.mixfa.excify.FastThrowable
import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.account.model.matchesById
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.security.Principal

fun Principal.throwIfNot(account: Account) {
    if (!this.matchesById(account)) throw FastAccessException.get()
}

object SecurityUtils {
    private val notAuthenticatedException = FastThrowable("User not authenticated")

    fun authenticatedPrincipal(): Authentication =
        SecurityContextHolder.getContext().authentication ?: throw notAuthenticatedException
}