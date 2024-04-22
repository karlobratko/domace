package hr.algebra.domace.domain.security

import kotlin.time.Duration

sealed interface Token {
    val value: String

    @JvmInline value class Refresh(override val value: String) : Token

    @JvmInline value class Access(override val value: String) : Token

    @JvmInline value class Lasting(val value: Duration)

    data class Pair(val refresh: Refresh, val access: Access)
}
