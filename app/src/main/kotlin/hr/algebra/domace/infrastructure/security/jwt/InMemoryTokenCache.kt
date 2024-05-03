package hr.algebra.domace.infrastructure.security.jwt

import arrow.core.Option
import arrow.core.toOption
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import hr.algebra.domace.domain.security.jwt.Token
import hr.algebra.domace.domain.security.jwt.TokenCache
import kotlin.time.TimeSource

private const val CACHE_MAX_SIZE: Long = 100 * 1024 * 1024 // 100 MB

fun <T : Any> InMemoryTokenCache(
    expireAfter: Token.Lasting,
    maxSize: Long = CACHE_MAX_SIZE,
    timeSource: TimeSource = TimeSource.Monotonic
) = object : TokenCache<T> {
    private val cache = InMemoryKache<Token, T>(maxSize = maxSize) {
        strategy = KacheStrategy.LRU
        this.timeSource = timeSource
        expireAfterWriteDuration = expireAfter.value
    }

    override suspend fun put(token: Token, value: T): Option<T> = cache.put(token, value).toOption()

    override suspend fun get(token: Token): Option<T> = cache.get(token).toOption()

    override suspend fun revoke(token: Token): Option<T> = cache.remove(token).toOption()
}
