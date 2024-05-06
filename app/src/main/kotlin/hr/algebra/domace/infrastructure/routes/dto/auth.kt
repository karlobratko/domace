package hr.algebra.domace.infrastructure.routes.dto

import hr.algebra.domace.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: User.Role,
    val redirectUrl: String
)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val accessToken: String, val refreshToken: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class RefreshResponse(val accessToken: String, val refreshToken: String)

@Serializable
data class LogoutRequest(val refreshToken: String)
