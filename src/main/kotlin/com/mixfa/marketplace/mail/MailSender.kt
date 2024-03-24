package com.mixfa.marketplace.mail

interface MailSender {
    fun sendSimpleEmail(to: String, subject: String, text: String)
}

