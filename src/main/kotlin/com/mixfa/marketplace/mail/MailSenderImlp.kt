package com.mixfa.marketplace.mail

import com.mixfa.marketplace.shared.DEFAULT_FIXED_RATE
import com.mixfa.marketplace.shared.runCatchLog
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
class MailSenderImlp(
    private val emailSender: JavaMailSender,
    @Value("\${mail.from}") private val from: String
) : MailSender {
    private val mails = Collections.synchronizedMap(PassiveExpiringMap<String, String>(5, TimeUnit.MINUTES))

    @Scheduled(fixedRate = DEFAULT_FIXED_RATE)
    fun clearExpiredEmails() {
        mails.size
    }

    override fun sendSimpleEmail(to: String, subject: String, text: String) {
        if (mails.containsKey(to))
            throw EmailThrottlingException.get()

        logger.info("Sending email to {} text {}", to, text)
        runCatchLog(logger) {
            emailSender.send(SimpleMailMessage().apply {
                this.setTo(to)
                this.from = this@MailSenderImlp.from
                this.subject = subject
                this.text = text
            })

            mails.put(to, "")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}