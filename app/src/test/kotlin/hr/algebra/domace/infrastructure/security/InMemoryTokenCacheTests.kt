package hr.algebra.domace.infrastructure.security

import hr.algebra.domace.domain.TestTimeSource
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.Token
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.ShouldSpec
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

object InMemoryTokenCacheTests : ShouldSpec({

    should("put and get Token and User.Id to cache") {
        val lasting = Token.Lasting(15.minutes)
        val cache = InMemoryTokenCache(lasting)

        cache.put(TOKEN_1, USERID_1)

        cache.get(TOKEN_1) shouldBeSome USERID_1
    }

    should("overwrite old User.Id if same Token is put") {
        val lasting = Token.Lasting(15.minutes)
        val cache = InMemoryTokenCache(lasting)

        cache.put(TOKEN_1, USERID_1)

        cache.get(TOKEN_1) shouldBeSome USERID_1

        cache.put(TOKEN_1, USERID_2)

        cache.get(TOKEN_1) shouldBeSome USERID_2
    }

    should("expire after specified period of time") {
        val testTimeSource = TestTimeSource()

        val lasting = Token.Lasting(15.milliseconds)
        val cache = InMemoryTokenCache(lasting, timeSource = testTimeSource)

        cache.put(TOKEN_1, USERID_1)

        testTimeSource += lasting.value

        cache.get(TOKEN_1).shouldBeNone()
    }

    should("revoke token") {
        val lasting = Token.Lasting(15.minutes)
        val cache = InMemoryTokenCache(lasting)

        cache.put(TOKEN_1, USERID_1)

        cache.get(TOKEN_1) shouldBeSome USERID_1

        cache.revoke(TOKEN_1)

        cache.get(TOKEN_1).shouldBeNone()
    }
})

private val TOKEN_1 = Token.Access("first")
private val USERID_1 = User.Id(1)
private val USERID_2 = User.Id(2)
