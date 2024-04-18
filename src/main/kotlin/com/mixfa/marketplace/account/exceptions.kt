package com.mixfa.marketplace.account

import com.mixfa.excify.ExcifyCachedException
import com.mixfa.excify.FastThrowable

@ExcifyCachedException
class AdminSecretIsNullException : FastThrowable("Requested admin role but admin secret is null") {
    companion object
}

@ExcifyCachedException
class UsernameNotValidException : FastThrowable("Username not valid") {
    companion object
}