package com.amwaal.amwall_app.users
import com.amwaal.amwall_app.auth.RegisterRequestDto
import com.amwaal.amwall_app.users.User
import com.amwaal.amwall_app.users.UserResponseDto
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    @Transactional
    fun registerNewUser(dto: RegisterRequestDto): UserResponseDto {
        if (userRepository.existsByEmail(dto.email)) {
            throw IllegalArgumentException("Email is already registered!")
        }
        
        if(userRepository.existsByPhoneNumber(dto.phoneNumber)) {
            throw IllegalArgumentException("Phone number is already registered!")
        }

        if(userRepository.existsByUsername(dto.username)) {
            throw IllegalArgumentException("Username is already taken!")
        }
        
        val hashedPassword = requireNotNull(passwordEncoder.encode(dto.password))

        val newUser = User(
            email = dto.email,
            password = hashedPassword,
            firstName = dto.firstName,
            lastName = dto.lastName,
            role = Role.USER,
            username = dto.username,
            phoneNumber = dto.phoneNumber,
        )

        val savedUser = userRepository.save(newUser)

        return UserResponseDto(
            id = requireNotNull(savedUser.id),
            email = savedUser.email,
            role = savedUser.role,
            firstName = savedUser.firstName,
            lastName = savedUser.lastName,
            hasSetupPin = savedUser.hasSetupPin,
            phoneNumber = savedUser.phoneNumber,
            phoneVerified = savedUser.phoneVerified,
            emailVerified = savedUser.emailVerified,
            createdAt = savedUser.createdAt ?: throw IllegalStateException("CreatedAt timestamp should not be null for a saved user")
        )
    }

    fun findUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    fun updateUserPin(userId: Long, newPin: String): UserResponseDto {
        val user = userRepository.findById(userId).orElseThrow { 
            IllegalArgumentException("User with ID $userId not found") 
        }
        
        val hashedPin = passwordEncoder.encode(newPin)
        user.pin = hashedPin
        user.hasSetupPin = true
        val updatedUser = userRepository.save(user)
        
        return updatedUser.toResponseDto()
    }
    
    fun isEmailRegistered(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }
    
    fun isPhoneNumberRegistered(phoneNumber: String): Boolean {
        return userRepository.existsByPhoneNumber(phoneNumber)
    }
    
    fun isUsernameRegistered(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }
}
