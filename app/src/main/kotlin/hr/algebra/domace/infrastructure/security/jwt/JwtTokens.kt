package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.flatMap
import arrow.core.nonEmptyListOf
import arrow.core.toEitherNel
import hr.algebra.domace.domain.SecurityError
import hr.algebra.domace.domain.SecurityError.ClaimsExtractionError
import hr.algebra.domace.domain.SecurityError.TokenGenerationError
import hr.algebra.domace.domain.SecurityError.TokenVerificationError
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.Tokens
import hr.algebra.domace.infrastructure.security.Secret
import io.github.nefilim.kjwt.JWSHMAC512Algorithm
import io.github.nefilim.kjwt.JWT
import io.github.nefilim.kjwt.sign
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json

private val JwtJsonFormat = Json

fun JwtTokens(secret: Secret) =
    object : Tokens {
        override suspend fun Claims.generateToken(): Either<SecurityError, Token> =
            JWT
                .hs512 {
                    issuer(issuer.value)
                    subject(subject.value)
                    audience(audience.map { it.value }, JwtJsonFormat)
                    use(use.name)
                    issuedAt(issuedAt.value.toJavaInstant())
                    expiresAt(expiresAt.value.toJavaInstant())
                }
                .sign(secret.value)
                .mapLeft { TokenGenerationError }
                .map {
                    when (this) {
                        is Claims.Refresh -> Token.Refresh(it.rendered)
                        is Claims.Access -> Token.Access(it.rendered)
                    }
                }

        override suspend fun Token.extractClaims(): EitherNel<SecurityError, Claims> =
            JWT
                .decodeT(value, JWSHMAC512Algorithm)
                .mapLeft { nonEmptyListOf(TokenVerificationError) }
                .flatMap { decoded ->
                    with(JwtValidation<JWSHMAC512Algorithm>()) {
                        decoded.validate()
                    }
                }
                .flatMap { validated ->
                    with(JwtToTokenClaimsConversion<JWSHMAC512Algorithm>(JwtJsonFormat)) {
                        validated.convert().mapLeft { ClaimsExtractionError }.toEitherNel()
                    }
                }
    }
