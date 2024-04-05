package com.mixfa.marketplace.account

import com.mixfa.excify.FastThrowable

class UsernameTakenException(username: String) : FastThrowable("Username $username is already in use") {
}

class EmailTakenException(email: String) : FastThrowable("Email $email is already in use") {
}

class AdminCreationException(enteredSecret: String) : FastThrowable("Can`t create admin using $enteredSecret") {
}

class EmailCodeNotValidException(code: String) :
    FastThrowable("Code $code is not related to any email address or expired") {
}