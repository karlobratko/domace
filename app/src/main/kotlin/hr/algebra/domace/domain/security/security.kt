package hr.algebra.domace.domain.security

data class Security(
    val issuer: Claims.Issuer,
    val refreshLasting: Token.Lasting,
    val accessLasting: Token.Lasting
)

@JvmInline value class Secret(val value: String)
