package com.mixfa.marketplace.mail

import com.mixfa.excify.ExcifyCachedException
import com.mixfa.excify.FastException

@ExcifyCachedException
class EmailThrottlingException : FastException("Email to this address was already sent") {
    companion object
}