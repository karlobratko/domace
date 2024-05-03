package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.Option
import io.github.nefilim.kjwt.DecodedJWT
import io.github.nefilim.kjwt.JWSAlgorithm
import io.github.nefilim.kjwt.JWT.Companion.JWTClaimSetBuilder
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

const val USE_CLAIM_NAME = "use"

const val ROLE_CLAIM_NAME = "role"

fun JWTClaimSetBuilder.audience(audience: List<String>, format: StringFormat) =
    audience(format.encodeToString(ListSerializer(String.serializer()), audience))

fun JWTClaimSetBuilder.use(value: String) = claim(USE_CLAIM_NAME, value)

fun <T : JWSAlgorithm> DecodedJWT<T>.use(): Option<String> = claimValue(USE_CLAIM_NAME)

fun JWTClaimSetBuilder.role(value: String) = claim(ROLE_CLAIM_NAME, value)

fun <T : JWSAlgorithm> DecodedJWT<T>.role(): Option<String> = claimValue(ROLE_CLAIM_NAME)
