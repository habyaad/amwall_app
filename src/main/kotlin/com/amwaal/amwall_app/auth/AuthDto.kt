package com.amwaal.amwall_app.auth

import com.amwaal.amwall_app.users.User
import com.amwaal.amwall_app.users.UserResponseDto
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Length
import jakarta.validation.constraints.Pattern

enum class FieldType {
    Email,
    PhoneNumber,
    Username
}

data class RegisterRequestDto(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    val password: String,

    @field:NotBlank(message = "First name cannot be blank")
    @field:Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    val firstName: String,

    @field:NotBlank(message = "Last name cannot be blank")
    @field:Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    val lastName: String,

    @field:NotBlank(message = "Phone number cannot be blank")
    @field:Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}$", // E.164 standard phone format
        message = "Phone number must be a valid international format (e.g. +1234567890)"
    )
    val phoneNumber: String,

    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    val username: String,
    
    @field:NotBlank(message = "Country code cannot be blank")
    @field:Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    val countryCode: String,
    
    @field:NotBlank(message = "Gender cannot be blank")
    @field:Pattern(
        regexp = "^(Male|Female|Other)$",
        message = "Field must be one of: Male, Female, Other"
    )    val gender: String
    
)

data class LoginRequestDto(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 digits")
    val pin: String?,

    @field:Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    val password: String?
) {
    @AssertTrue(message = "Either PIN or password must be provided not both")
    @JsonIgnore
    @Schema(hidden = true)
    fun isAuthenticationMethodProvided(): Boolean {
        val hasPin = !pin.isNullOrBlank()
        val hasPassword = !password.isNullOrBlank()
        return hasPin xor hasPassword
    }
}

data class AuthResponseDto(
    val message: String,
    val accessToken: String,
    val user: UserResponseDto
)

data class SetPinRequestDto(
    @NotBlank(message = "PIN cannot be blank")
    @field:Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 digits")
    val pin: String,

    @NotBlank(message = "Confirm PIN cannot be blank")
    @field:Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 digits")
    val confirmPin: String
) {
    @AssertTrue(message = "Set PIN and confirm PIN must be the same")
    @JsonIgnore
    @Schema(hidden = true)
    fun isEquals(): Boolean {
        return pin == confirmPin
    }
}

data class CheckAvailabilityRequestDto(
    @field:NotBlank(message = "Value cannot be blank")
    val value: String,

    @field:Pattern(
        regexp = "^(Email|PhoneNumber|Username)$",
        message = "Field must be one of: Email, PhoneNumber, Username"
    )
    val field: String,
)