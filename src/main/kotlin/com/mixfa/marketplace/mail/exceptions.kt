package com.mixfa.marketplace.mail

import com.mixfa.excify.ExcifyException
import com.mixfa.excify.FastThrowable

@ExcifyException
class EmailThrottlingException : FastThrowable("Email to this address was already sent") {
    companion object
}