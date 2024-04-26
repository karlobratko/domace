package hr.algebra.domace.infrastructure.security.kjwt

import arrow.core.Option
import io.github.nefilim.kjwt.DecodedJWT
import io.github.nefilim.kjwt.JWSAlgorithm
import io.github.nefilim.kjwt.JWT.Companion.JWTClaimSetBuilder
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

fun JWTClaimSetBuilder.audience(audience: List<String>, format: StringFormat) =
    audience(format.encodeToString(ListSerializer(String.serializer()), audience))

fun JWTClaimSetBuilder.use(value: String) = claim("use", value)

fun <T : JWSAlgorithm> DecodedJWT<T>.use(): Option<String> = claimValue("use")
