package com.mixfa.marketplace.shared

import com.mixfa.excify.FastException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

const val IS_AUTHENTICATED = "isAuthenticated() == true"

private val accessDeniedException = FastException("Access denied")

fun Principal.throwIfNot(account: UserDetails) {
    if (this.name != account.username) throw accessDeniedException
}

private val notAuthenticatedException = FastException("User not authenticated")

fun authenticatedPrincipal(): Authentication =
    SecurityContextHolder.getContext().authentication ?: throw notAuthenticatedException
