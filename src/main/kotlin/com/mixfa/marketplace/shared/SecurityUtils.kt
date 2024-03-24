package com.mixfa.marketplace.shared

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder


object SecurityUtils {
    private val notAuthenticatedException = Exception("User not authenticated")

    fun getAuthenticatedPrincipal(): Authentication =
        SecurityContextHolder.getContext().authentication ?: throw notAuthenticatedException
}