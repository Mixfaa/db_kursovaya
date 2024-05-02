package com.mixfa.marketplace.account

import com.mixfa.excify.ExcifyCachedException
import com.mixfa.excify.FastException

@ExcifyCachedException
class AdminSecretIsNullException : FastException("Requested admin role but admin secret is null") {
    companion object
}
