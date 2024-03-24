package com.mixfa.marketplace.mail

import FastThrowable
import org.apache.commons.collections4.map.PassiveExpiringMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class MailSenderImlp(
    private val emailSender: JavaMailSender,
    @Value("\${mail.from}") private val from: String
) : MailSender {
    private val mails = Collections.synchronizedMap(PassiveExpiringMap<String, String>(5, TimeUnit.MINUTES))

    override fun sendSimpleEmail(to: String, subject: String, text: String) {
        if (mails.containsKey(to))
            throw FastThrowable("Email to this address was already sent")

        println("Sending email to $to text: $text")
        emailSender.send(SimpleMailMessage().apply {
            this.setTo(to)
            this.from = this@MailSenderImlp.from
            this.subject = subject
            this.text = text
        })

        mails.put(to, "")
    }
}