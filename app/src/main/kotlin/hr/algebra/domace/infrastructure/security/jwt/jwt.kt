package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.Option
import io.github.nefilim.kjwt.DecodedJWT
import io.github.nefilim.kjwt.JWSAlgorithm
import io.github.nefilim.kjwt.JWT.Companion.JWTClaimSetBuilder
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

fun JWTClaimSetBuilder.audience(
    audience: List<String>,
    format: StringFormat = Json
) = audience(format.encodeToString(ListSerializer(String.serializer()), audience))

fun JWTClaimSetBuilder.use(value: String) = claim("use", value)

fun <T : JWSAlgorithm> DecodedJWT<T>.use(): Option<String> = claimValue("use")
