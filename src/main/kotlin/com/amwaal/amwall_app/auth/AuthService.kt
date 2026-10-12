package com.amwaal.amwall_app.auth


import com.amwaal.amwall_app.auth.RegisterRequestDto
import com.amwaal.amwall_app.infrastructure.security.TokenService
import com.amwaal.amwall_app.infrastructure.security.TokenUserPrincipal
import com.amwaal.amwall_app.users.UserResponseDto
import com.amwaal.amwall_app.users.UserService
import com.amwaal.amwall_app.users.toResponseDto
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val tokenService: TokenService,
) {

    fun register(dto: RegisterRequestDto): AuthResponseDto {
        val newUser = userService.registerNewUser(dto)
        return AuthResponseDto(
            "Account successfully created",
            "",
            newUser
        )
    }

    fun login(dto: LoginRequestDto): AuthResponseDto {

        val credentialInput = dto.password ?: dto.pin
        ?: throw BadCredentialsException("Either password or PIN must be provided")

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(dto.email, credentialInput)
        )

        val userEntity = userService.findUserByEmail(dto.email)
            ?: throw BadCredentialsException("Invalid credentials")

        val principal = TokenUserPrincipal(
            id = userEntity.id!!,
            username = userEntity.email,
            role = userEntity.role
        )

        val token = tokenService.generateToken(principal)
        return AuthResponseDto(
            message = "Login successful",
            accessToken = token,
            user = userEntity.toResponseDto()
        )
    }

    fun setPin(dto: SetPinRequestDto, principal: TokenUserPrincipal): AuthResponseDto {
        if (dto.pin != dto.confirmPin) {
            throw IllegalArgumentException("PIN and Confirm PIN must match")
        }

        val updatedUser = userService.updateUserPin(principal.id, dto.pin)

        return AuthResponseDto(
            message = "PIN set successfully",
            accessToken = "", //TODO: Consider returning a new token later if needed
            user = updatedUser
        )
    }
    
    fun checkAvailability(dto: CheckAvailabilityRequestDto): Map<String, Boolean> {
        val result = when (dto.field) {
            "Email" -> !userService.isEmailRegistered(dto.value)
            "PhoneNumber" -> !userService.isPhoneNumberRegistered(dto.value)
            "Username" -> !userService.isUsernameRegistered(dto.value)
            else -> throw IllegalArgumentException("Invalid field type: ${dto.field}")
        }
        
        return mapOf("isAvailable" to result)
    }
}
