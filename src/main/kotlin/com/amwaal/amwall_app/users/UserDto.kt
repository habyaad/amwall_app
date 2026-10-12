package com.amwaal.amwall_app.users

import java.time.LocalDateTime

data class UserResponseDto(
    val id: Long,
    val email: String,
    val role: Role,
    val firstName: String,
    val lastName: String,
    val hasSetupPin: Boolean,
    val phoneNumber: String,
    val phoneVerified: Boolean,
    val emailVerified: Boolean,
    val country: String,
    val gender: Gender,
    val createdAt: LocalDateTime,
)

fun User.toResponseDto(): UserResponseDto {
    return UserResponseDto(
        id = requireNotNull(this.id) { "User ID must be set before creating a response DTO" },
        email = this.email,
        role = this.role,
        firstName = this.firstName,
        lastName = this.lastName,
        phoneNumber = this.phoneNumber,
        hasSetupPin =hasSetupPin,
        phoneVerified = this.phoneVerified,
        emailVerified = this.emailVerified,
        country = this.country,
        gender = this.gender,
        createdAt = this.createdAt ?: LocalDateTime.now() 
    )
}
