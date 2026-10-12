package com.amwaal.amwall_app.auth

import com.amwaal.amwall_app.auth.RegisterRequestDto
import com.amwaal.amwall_app.infrastructure.security.TokenUserPrincipal
import com.amwaal.amwall_app.users.UserResponseDto
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequestDto): ResponseEntity<AuthResponseDto> {
        val registeredUser = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequestDto): ResponseEntity<AuthResponseDto> {
        try {
            val loggedInUser = authService.login(request)
            return ResponseEntity.ok<AuthResponseDto>(loggedInUser)
        } catch (ex: Exception) {
            throw ex
        }
    }

    @PostMapping("/set-pin")
    @SecurityRequirement(name = "BearerAuth") // Swagger
    fun setPin(
        @Valid @RequestBody request: SetPinRequestDto,
        @AuthenticationPrincipal principal: TokenUserPrincipal
    ): ResponseEntity<AuthResponseDto> {
        try {
            val loggedInUser = authService.setPin(request, principal)
            return ResponseEntity.ok<AuthResponseDto>(loggedInUser)
        } catch (ex: Exception) {
            throw ex
        }
    }

    @PostMapping("/check-availability")
    fun checkAvailability(@Valid @RequestBody request: CheckAvailabilityRequestDto): ResponseEntity<Map<String, Boolean>> {
        val isAvailable = authService.checkAvailability(request)
        return ResponseEntity.ok(isAvailable)
    }

    // Direct error translation helper for this controller context
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("error" to (ex.message ?: "Invalid request")))
    }
}
