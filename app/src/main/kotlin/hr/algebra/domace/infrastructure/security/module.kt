package hr.algebra.domace.infrastructure.security

import hr.algebra.domace.domain.security.AuthContext
import hr.algebra.domace.domain.security.LastingFor
import hr.algebra.domace.domain.security.Secret
import hr.algebra.domace.domain.security.Security
import hr.algebra.domace.domain.security.jwt.Claims
import hr.algebra.domace.domain.security.jwt.JwtTokenService
import hr.algebra.domace.infrastructure.security.authentication.JwtAuthenticationScope
import hr.algebra.domace.infrastructure.security.jwt.JwtTokens
import hr.algebra.domace.infrastructure.security.jwt.SecurityConfig
import hr.algebra.domace.infrastructure.security.jwt.SecuritySecretConfig
import hr.algebra.domace.infrastructure.serialization.Resources
import org.koin.core.qualifier.named
import org.koin.dsl.module

val SecurityModule =
    module {
        single(createdAtStart = true) {
            val config: SecurityConfig = Resources.hocon("security/security.conf")

            Security(
                issuer = Claims.Issuer(config.issuer),
                refreshLasting = LastingFor(config.lasting.refresh),
                accessLasting = LastingFor(config.lasting.access)
            )
        }

        single(createdAtStart = true) {
            val config: SecuritySecretConfig = Resources.hocon("secrets/security.dev.conf")

            Secret(config.secret)
        }

        single {
            InMemoryTokenCache<AuthContext>(
                expireAfter = get<Security>().accessLasting
            )
        }

        single { JwtTokens(get<Secret>()) }

        single {
            JwtTokenService(
                security = get(),
                algebra = get(),
                refreshTokenPersistence = get(),
                accessTokenCache = get()
            )
        }

        single(named("jwt")) { JwtAuthenticationScope(get()) }
    }
