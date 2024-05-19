package com.mixfa.account.model

enum class Permission {
    FILES_EDIT,
    MARKETPLACE_EDIT,
    COMMENTS_EDIT,
    FAVLIST_EDIT,
    ORDERS_EDIT;

    val normalName = this.name.replace('_',':')
}
