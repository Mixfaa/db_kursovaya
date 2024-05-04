package com.mixfa.marketplace.account.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

@Document("account")
data class Account(
    @Id private val username: String,
    val firstname: String,
    val lastname: String,

    @field:JsonIgnore
    val email: String,

    @field:JsonIgnore
    private val password: String,
    val role: Role,
    @field:JsonIgnore
    val shippingAddresses: List<String> = listOf(),
) : UserDetails {
    override fun getUsername(): String = username

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = role.grantedAuthorities

    @JsonIgnore
    override fun getPassword(): String = password

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean = true

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean = true

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean = true

    @JsonIgnore
    override fun isEnabled(): Boolean = true

    data class RegisterRequest(
        @field:NotBlank
        val username: String,
        @field:NotBlank
        val firstname: String,
        @field:NotBlank
        val lastname: String,
        @field:Length(min = 8, max = 25)
        val password: String,
        @field:NotBlank
        val role: String,
        @field:NotBlank
        val mailCode: String,
        val adminSecret: String? = null
    )

    fun toPrivateDetails() = PrivateDetails(username, firstname, lastname, email, role, shippingAddresses)

    data class PrivateDetails(
        val username: String,
        val firstname: String,
        val lastname: String,
        val email: String,
        val role: Role,
        val shippingAddresses: List<String>,
    )
}
