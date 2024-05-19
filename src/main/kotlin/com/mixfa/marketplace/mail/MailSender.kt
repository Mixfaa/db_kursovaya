package com.mixfa.marketplace.mail

import com.mixfa.`excify-either`.makeMemorizedException
import com.mixfa.shared.DEFAULT_FIXED_RATE
import com.mixfa.shared.launchIO
import kotlinx.coroutines.GlobalScope
import org.apache.commons.collections4.map.PassiveExpiringMap
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class MailSender(
    private val emailSender: JavaMailSender,
    @Value("\${mail.from}") private val from: String
) {
    private val mails = Collections.synchronizedMap(PassiveExpiringMap<String, String>(5, TimeUnit.MINUTES))

    @Scheduled(fixedRate = DEFAULT_FIXED_RATE)
    fun clearExpiredEmails() {
        mails.size
    }

    @Throws
    fun sendMail(to: String, subject: String, text: String) {
        if (mails.containsKey(to))
            throw makeMemorizedException("Email to this address was already sent")

        logger.info("Sending email to {} text {}", to, text)

        GlobalScope.launchIO {
            emailSender.send(SimpleMailMessage().apply {
                this.setTo(to)
                this.from = this@MailSender.from
                this.subject = subject
                this.text = text
            })

            mails.remove(to)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}