package com.mixfa.marketplace.mail

import com.mixfa.excify.ExcifyCachedException
import com.mixfa.excify.FastThrowable

@ExcifyCachedException
class EmailThrottlingException : FastThrowable("Email to this address was already sent") {
    companion object
}