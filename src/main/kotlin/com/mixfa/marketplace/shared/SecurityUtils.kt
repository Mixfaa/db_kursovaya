package com.mixfa.marketplace.shared

import com.mixfa.excify.FastThrowable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder


object SecurityUtils {
    private val notAuthenticatedException = FastThrowable("User not authenticated")

    fun getAuthenticatedPrincipal(): Authentication =
        SecurityContextHolder.getContext().authentication ?: throw notAuthenticatedException
}