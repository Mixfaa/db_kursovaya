package com.mixfa

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.mixfa.shared.converter.WithDtoSerializer
import com.mixfa.shared.model.WithDto
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.transaction.TransactionManager
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@SpringBootApplication
@EnableMongoRepositories
@EnableMethodSecurity
@EnableWebSecurity
@EnableScheduling
class DbKursovayaApplication {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityWebFilterChain(
        http: HttpSecurity,
    ): SecurityFilterChain =
        http.httpBasic(Customizer.withDefaults())
            .csrf { it.disable() }
            .cors {
                it.configurationSource(UrlBasedCorsConfigurationSource().apply {
                    registerCorsConfiguration(
                        "/**",
                        CorsConfiguration().applyPermitDefaultValues().apply {
                            allowedMethods = listOf("GET", "POST", "DELETE", "PUT")
                        }
                    )
                })
            }
            .build()


    @Bean
    fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder
                .serializerByType(ObjectId::class.java, ToStringSerializer())
                .serializerByType(WithDto::class.java, WithDtoSerializer())
                .featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .modules(kotlinModule())
                .postConfigurer {
                    it.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
        }
    }

    @Bean
    fun transactionManager(factory: MongoDatabaseFactory): TransactionManager = MongoTransactionManager(factory)

    @Bean
    fun validator(): LocalValidatorFactoryBean = LocalValidatorFactoryBean()

    @Bean
    fun validationPostProcessor(): MethodValidationPostProcessor {
        val processor = MethodValidationPostProcessor()
        processor.setAdaptConstraintViolations(true)
        return processor
    }

    @Bean
    fun getJavaMailSender(
        @Value("\${spring.mail.host}") host: String,
        @Value("\${spring.mail.port}") port: Int,
        @Value("\${spring.mail.username}") username: String,
        @Value("\${spring.mail.password}") password: String,
    ): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = host
        mailSender.port = port

        mailSender.username = username
        mailSender.password = password

        val props = mailSender.javaMailProperties
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.debug"] = "true"

        return mailSender
    }
}


fun main(args: Array<String>) {
    runApplication<DbKursovayaApplication>(*args)
}
