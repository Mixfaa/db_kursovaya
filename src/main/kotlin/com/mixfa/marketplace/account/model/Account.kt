package com.mixfa.marketplace.account.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

@Document("account")
data class Account(
    @Id @field:JvmField val username: String,
    val firstname: String,
    val lastname: String,

    @field:JsonIgnore
    val email: String,

    @field:JvmField
    @field:JsonIgnore
    val password: String,
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
        @NotBlank
        val username: String,
        @NotBlank
        val firstname: String,

        @NotBlank
        val lastname: String,

        @Length(min = 8, max = 25)
        val password: String,

        @NotBlank
        val role: String,

        @NotBlank
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

fun Principal.matchesById(account: Account): Boolean = this.name == account.username
