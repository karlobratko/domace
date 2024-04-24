package hr.algebra.domace.infrastructure.security

import arrow.core.Option
import arrow.core.toOption
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import hr.algebra.domace.domain.security.Token
import hr.algebra.domace.domain.security.TokenCache
import kotlin.time.TimeSource

private const val CACHE_MAX_SIZE: Long = 100 * 1024 * 1024 // 100 MB

fun <Subject : Any> InMemoryTokenCache(
    expireAfter: Token.Lasting,
    maxSize: Long = CACHE_MAX_SIZE,
    timeSource: TimeSource = TimeSource.Monotonic
) = object : TokenCache<Subject> {
    private val cache = InMemoryKache<Token, Subject>(maxSize = maxSize) {
        strategy = KacheStrategy.LRU
        this.timeSource = timeSource
        expireAfterWriteDuration = expireAfter.value
    }

    override suspend fun put(token: Token, claims: Subject): Option<Subject> = cache.put(token, claims).toOption()

    override suspend fun get(token: Token): Option<Subject> = cache.get(token).toOption()

    override suspend fun revoke(token: Token): Option<Subject> = cache.remove(token).toOption()
}
