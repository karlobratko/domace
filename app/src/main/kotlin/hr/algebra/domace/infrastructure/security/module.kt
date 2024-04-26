package hr.algebra.domace.infrastructure.security

import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.persistence.RefreshTokenPersistence
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.security.Secret
import hr.algebra.domace.domain.security.Security
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.TokenCache
import hr.algebra.domace.domain.security.Tokens
import hr.algebra.domace.domain.security.jwt.JwtTokenService
import hr.algebra.domace.infrastructure.security.kjwt.JwtTokens
import hr.algebra.domace.infrastructure.serialization.Resources
import org.koin.dsl.module

val SecurityModule =
    module {
        single(createdAtStart = true) {
            val config: SecurityConfig = Resources.hocon("security/security.conf")

            println(config)

            Security(
                issuer = Claims.Issuer(config.issuer),
                refreshLasting = Token.Lasting(config.lasting.refresh),
                accessLasting = Token.Lasting(config.lasting.access)
            )
        }

        single(createdAtStart = true) {
            val config: SecuritySecretConfig = Resources.hocon("secrets/security.dev.conf")

            Secret(config.secret)
        }

        single {
            InMemoryTokenCache<User.Id>(
                expireAfter = get<Security>().accessLasting
            )
        }

        single { JwtTokens(get<Secret>()) }

        single {
            JwtTokenService(
                security = get<Security>(),
                algebra = get<Tokens>(),
                refreshTokenPersistence = get<RefreshTokenPersistence>(),
                accessTokenCache = get<TokenCache<User.Id>>()
            )
        }
    }
