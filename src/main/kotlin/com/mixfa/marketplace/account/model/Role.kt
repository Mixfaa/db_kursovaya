package com.mixfa.marketplace.account.model

import org.springframework.security.core.authority.SimpleGrantedAuthority

enum class Role(
    permissions: Set<Permission>
) {
    ADMIN(
        setOf(
            Permission.FILES_READ, Permission.FILES_WRITE,
            Permission.MARKETPLACE_READ, Permission.MARKETPLACE_WRITE,
        )
    ),
    CUSTOMER(
        setOf(
            Permission.FILES_READ, Permission.FILES_WRITE,
            Permission.MARKETPLACE_READ,
            Permission.COMMENTS_READ, Permission.COMMENTS_WRITE,
            Permission.FAVLIST_READ, Permission.FAVLIST_WRITE,
            Permission.ORDER_READ, Permission.ORDER_WRITE
        )
    ),
    GUEST(
        setOf(
            Permission.FILES_READ,
            Permission.MARKETPLACE_READ,
            Permission.FAVLIST_READ,
            Permission.COMMENTS_READ,
        )
    );

    val grantedAuthorities: MutableList<SimpleGrantedAuthority> =
        permissions.mapTo(mutableListOf()) { SimpleGrantedAuthority(it.name.replace('_', ':')) }
            .also { it.add(SimpleGrantedAuthority("ROLE_${this.name}")) }
}