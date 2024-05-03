package hr.algebra.domace.domain.mailing

import arrow.core.Nel
import io.ktor.http.ContentType
import io.ktor.http.withCharset
import java.nio.charset.Charset
import kotlinx.serialization.Serializable

private val UTF8 = Charset.forName("UTF-8")

/**
 * Represents an Email.
 *
 * @property from The sender of the email.
 * @property to The recipients of the email.
 * @property subject The subject of the email.
 * @property content The content of the email.
 */
data class Email(
    val from: Participant,
    val to: Nel<Participant>,
    val subject: Subject,
    val content: Content
) {
    /**
     * Represents the address of an email participant.
     *
     * @property value The address value.
     */
    @Serializable
    @JvmInline value class Address(val value: String)

    /**
     * Represents a participant in an email.
     *
     * @property address The address of the participant.
     * @property name The name of the participant.
     */
    @Serializable
    data class Participant(val address: Address, val name: Name) {
        /**
         * Represents the name of a participant.
         *
         * @property value The name value.
         */
        @Serializable
        @JvmInline value class Name(val value: String)
    }

    /**
     * Represents the subject of an email.
     *
     * @property value The subject value.
     */
    @JvmInline value class Subject(val value: String)

    /**
     * Represents the content of an email.
     */
    sealed interface Content {
        val value: String
        val type: ContentType

        /**
         * Represents HTML content of an email.
         *
         * @property value The HTML content value.
         */
        data class Html(override val value: String) : Content {
            override val type = ContentType.Text.Html.withCharset(UTF8)
        }

        /**
         * Represents plain text content of an email.
         *
         * @property value The plain text content value.
         */
        data class Text(override val value: String) : Content {
            override val type = ContentType.Text.Plain.withCharset(UTF8)
        }
    }
}
