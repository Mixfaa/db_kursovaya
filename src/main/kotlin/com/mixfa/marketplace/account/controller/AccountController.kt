package com.mixfa.marketplace.account.controller

import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.account.service.AccountService
import com.mixfa.marketplace.shared.model.CheckedPageable
import com.mixfa.marketplace.shared.orThrow
import jakarta.validation.constraints.Email
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Deprecated("User AccountControllerV2")
@RestController
@RequestMapping("/account")
class AccountController(
    private val accountService: AccountService
) {
    @PostMapping("/register")
    fun register(@RequestBody request: Account.RegisterRequest) = accountService.register(request)

    @PostMapping("/add_shipping_address")
    fun addShippingAddress(shippingAddress: String) =
        accountService.addShippingAddress(shippingAddress)

    @PostMapping("/remove_shipping_address")
    fun removeShippingAddress(shippingAddress: String) =
        accountService.removeShippingAddress(shippingAddress)

    @GetMapping("/get_me")
    fun getMe(principal: Principal) =
        accountService.accountByPrincipal(principal).orThrow().toPrivateDetails()

    @GetMapping("/find_users")
    fun findUsers(query: String, page: Int, pageSize: Int) =
        accountService.findUsers(query, CheckedPageable(page, pageSize))

    @PostMapping("/send_email_code")
    fun sendEmailVerifyCode(@Email email: String) =
        accountService.sendEmailTo(email)
}