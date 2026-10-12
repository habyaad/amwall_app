package com.amwaal.amwall_app.users

import com.amwaal.amwall_app.infrastructure.security.TokenUserPrincipal
import com.amwaal.amwall_app.users.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/me")
    fun getUserProfile(@AuthenticationPrincipal principal: TokenUserPrincipal): UserResponseDto {
        return userService.findUserByEmail(principal.username)?.toResponseDto()
            ?: throw IllegalArgumentException("User not found")
    }
}