package com.amwaal.amwall_app.infrastructure.config

import com.amwaal.amwall_app.users.Role

class ExtendedUser(
    private val username: String,
    private val passwordHash: String,
    private val pinHash: String,
    private val role: Role
) : org.springframework.security.core.userdetails.UserDetails {
    
    override fun getPassword(): String = passwordHash 
    fun getPinHash(): String = pinHash
    
    override fun getUsername(): String = username
    override fun getAuthorities() = listOf(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_${role.name}"))
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}