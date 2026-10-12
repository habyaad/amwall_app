package com.amwaal.amwall_app.infrastructure.config

import com.amwaal.amwall_app.infrastructure.security.JwtAuthenticationFilter
import com.amwaal.amwall_app.users.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val swaggerEndpoints = arrayOf(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml"
        )

        val authEndpoints = arrayOf(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/check-availability"
        )
        http {
            csrf { disable() } // Disables CSRF for your stateless endpoints

            sessionManagement {
                // Set policy to STATELESS so Spring never creates cookies/sessions
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            authorizeHttpRequests {
                authorize("/error", permitAll)
                swaggerEndpoints.forEach { authorize(it, permitAll) }
                authEndpoints.forEach { authorize(it, permitAll) }

                // Enforce authentication requirements for everything else
                authorize(anyRequest, authenticated)
            }

            // Add  custom JWT filter right BEFORE the standard UsernamePasswordAuthenticationFilter
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        }

        return http.build()
    }

    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService {
        return UserDetailsService { email ->
            val user = userRepository.findByEmail(email)
                ?: throw UsernameNotFoundException("User not found with email: $email")

            // Map your database entity to Spring Security's expected User object
            ExtendedUser(
                username = user.email,
                passwordHash = user.password, 
                pinHash = user.pin ?: "",      
                role = user.role
            )
        }
    }

    @Bean
    fun authenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationProvider {
        return object : AuthenticationProvider {
            override fun authenticate(authentication: Authentication): Authentication {
                val email = authentication.name
                val rawCredentials = authentication.credentials.toString()

                val userDetails = userDetailsService.loadUserByUsername(email) as ExtendedUser

                // Check if it matches either the password hash OR the pin hash via BCrypt
                val matchesPassword = passwordEncoder.matches(rawCredentials, userDetails.password)
                val matchesPin = passwordEncoder.matches(rawCredentials, userDetails.getPinHash())

                if (matchesPassword || matchesPin) {
                    return UsernamePasswordAuthenticationToken(userDetails, rawCredentials, userDetails.authorities)
                }

                throw BadCredentialsException("Invalid Credentials")
            }

            override fun supports(authentication: Class<*>): Boolean {
                return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
            }
        }
    }

}

