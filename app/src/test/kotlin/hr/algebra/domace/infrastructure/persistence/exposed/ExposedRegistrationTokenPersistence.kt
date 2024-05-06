package hr.algebra.domace.infrastructure.persistence.exposed

import hr.algebra.domace.domain.DbError.RegistrationTokenAlreadyConfirmed
import hr.algebra.domace.domain.model.RegistrationToken.ConfirmationStatus.Confirmed
import hr.algebra.domace.domain.model.RegistrationToken.ConfirmationStatus.Unconfirmed
import hr.algebra.domace.domain.security.LastingFor
import hr.algebra.domace.infrastructure.persistence.Database.test
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.should
import io.kotest.matchers.types.shouldBeTypeOf
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

object ExposedRegistrationTokenPersistence : ShouldSpec({
    val registrationTokenPersistence = ExposedRegistrationTokenPersistence(
        test,
        RegistrationConfig(LastingFor(15.minutes))
    )

    beforeContainer {
        transaction(test) {
            create(RegistrationTokensTable)
        }
    }

    afterContainer {
        transaction(test) {
            drop(RegistrationTokensTable)
        }
    }

    context("registration token insertion and confirmation") {
        val tokenId = registrationTokenPersistence.insertAndGetToken()
            .also {
                should("after insertion, registration token should be persisted and not confirmed") {
                    registrationTokenPersistence.select(it)
                        .shouldBeSome()
                        .should {
                            it.confirmationStatus shouldBeEqual Unconfirmed
                        }
                }
            }

        val confirmTokenId = registrationTokenPersistence.confirm(tokenId)
            .shouldBeRight()
            .also {
                should("after confirmation, registration token should be confirmed") {
                    it shouldBeEqual tokenId

                    registrationTokenPersistence.select(it)
                        .shouldBeSome()
                        .should {
                            it.confirmationStatus.shouldBeTypeOf<Confirmed>()
                        }
                }
            }

        should("confirmation of confirmed token should result in error") {
            registrationTokenPersistence.confirm(confirmTokenId) shouldBeLeft RegistrationTokenAlreadyConfirmed
        }
    }
})
