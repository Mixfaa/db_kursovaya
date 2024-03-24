package com.mixfa.marketplace.account.model

import org.springframework.security.core.authority.SimpleGrantedAuthority

enum class Role(
    permissions: Set<Permission>
) {
    ADMIN(setOf(Permission.ADMIN_PERMISSIONS)),
    CUSTOMER(setOf(Permission.CUSTOMER_PERMISSIONS));

    val grantedAuthorities: MutableList<SimpleGrantedAuthority> =
        permissions.mapTo(mutableListOf()) { SimpleGrantedAuthority(it.name) }
            .also { it.add(SimpleGrantedAuthority("ROLE_${this.name}")) }
}