package hr.algebra.domace.infrastructure.security

import arrow.core.Option
import arrow.core.toOption
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.TokenCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun InMemoryTokenCache(expireAfter: Token.Lasting) =
    object : TokenCache {
        private val cache = InMemoryKache<Token, User.Id>(maxSize = 32 * 1024 * 1024) {
            strategy = KacheStrategy.LRU
            creationScope = CoroutineScope(Dispatchers.IO)
            expireAfterWriteDuration = expireAfter.value
            onEntryRemoved = { _, _, _, _ -> }
        }

        override suspend fun put(token: Token, claims: User.Id) {
            cache.evictExpired()
            cache.put(token, claims)
        }

        override suspend fun get(token: Token): Option<User.Id> {
            cache.evictExpired()
            return cache.get(token).toOption()
        }

        override suspend fun revoke(token: Token) {
            cache.remove(token)
            cache.evictExpired()
        }
    }
