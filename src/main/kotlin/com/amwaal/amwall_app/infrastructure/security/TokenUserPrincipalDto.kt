package com.amwaal.amwall_app.infrastructure.security

import com.amwaal.amwall_app.users.Role

data class TokenUserPrincipal(
    val id: Long,
    val username: String, //email is the username
    val role: Role
)
