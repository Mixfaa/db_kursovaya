package com.mixfa.marketplace.account

import com.mixfa.excify.ExcifyException
import com.mixfa.excify.FastThrowable

@ExcifyException
class UsernameTakenException(username: String) : FastThrowable("Username $username is already in use") {
    companion object
}

@ExcifyException
class EmailTakenException(email: String) : FastThrowable("Email $email is already in use") {
    companion object
}

@ExcifyException
class AdminCreationException(enteredSecret: String) : FastThrowable("Can`t create admin using $enteredSecret") {
    companion object
}

@ExcifyException
class EmailCodeNotValidException(code: String) :
    FastThrowable("Code $code is not related to any email address or expired") {
    companion object
}