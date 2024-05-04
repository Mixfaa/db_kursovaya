package com.mixfa.marketplace.shared

import com.mixfa.excify.FastException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

fun Principal.throwIfNot(account: UserDetails) {
    if (this.name != account.username) throw FastAccessException.get()
}

fun Principal.throwIfNot(principal: Principal) {
    if (this.name != principal.name) throw FastAccessException.get()
}

private val notAuthenticatedException = FastException("User not authenticated")

fun authenticatedPrincipal(): Authentication =
    SecurityContextHolder.getContext().authentication ?: throw notAuthenticatedException
