package hr.algebra.domace.infrastructure.routes

import arrow.core.Nel
import arrow.core.toNonEmptyListOrNull
import hr.algebra.domace.domain.config.DefaultInstantProvider
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.EncodeDefault.Mode.NEVER as Never

/**
 * A sealed class representing a response with a timestamp.
 * This class is serializable with a custom serializer, ResponseSerializer.
 *
 * @param A The type of the data in a successful response.
 */
@Serializable(with = ResponseSerializer::class)
sealed class Response<out A> {
    /**
     * The timestamp of the response, represented as the number of milliseconds since the Unix epoch.
     */
    val timestamp = DefaultInstantProvider.now().toEpochMilliseconds()

    /**
     * A data class representing a successful response.
     * This class is a subtype of Response.
     *
     * @param A The type of the data in the response.
     * @property data The data in the response.
     */
    data class Success<out A> private constructor(val data: A) : Response<A>() {
        companion object {
            /**
             * A function to create a new Success object cast as Response<A> for serialization purposes.
             *
             * @param data The data in the response.
             * @return A new Success object with the given data.
             */
            operator fun <A> invoke(data: A): Response<A> = Success(data)
        }
    }

    /**
     * A data class representing a failed response.
     * This class is a subtype of Response and contains a non-empty list of error messages.
     *
     * @property errors The non-empty list of error messages.
     */
    data class Failure private constructor(val errors: Nel<String>) : Response<Nothing>() {
        companion object {
            /**
             * A function to create a new Failure object cast as Response<Nothing> for serialization purposes.
             *
             * @param errors The non-empty list of error messages.
             * @return A new Failure object with the given error messages.
             */
            operator fun invoke(errors: Nel<String>): Response<Nothing> = Failure(errors)
        }
    }
}

private class ResponseSerializer<A>(tSerializer: KSerializer<A>) : KSerializer<Response<A>> {

    @Serializable
    @SerialName("Response")
    @OptIn(ExperimentalSerializationApi::class)
    data class ResponseSurrogate<A>(
        val status: Status,
        val timestamp: Long,
        @EncodeDefault(mode = Never) val data: A? = null,
        @EncodeDefault(mode = Never) val errors: List<String>? = null
    ) {
        enum class Status {
            @SerialName("success")
            Success,

            @SerialName("failure")
            Failure
        }
    }

    private val surrogateSerializer = ResponseSurrogate.serializer(tSerializer)

    override val descriptor: SerialDescriptor = surrogateSerializer.descriptor

    override fun deserialize(decoder: Decoder): Response<A> {
        val surrogate = surrogateSerializer.deserialize(decoder)
        return when (surrogate.status) {
            ResponseSurrogate.Status.Success ->
                if (surrogate.data != null) {
                    Response.Success(surrogate.data)
                } else {
                    throw SerializationException("Missing data for successful result.")
                }

            ResponseSurrogate.Status.Failure ->
                if (!surrogate.errors.isNullOrEmpty()) {
                    Response.Failure(surrogate.errors.toNonEmptyListOrNull()!!)
                } else {
                    throw SerializationException("Missing errors for failing result.")
                }
        }
    }

    override fun serialize(encoder: Encoder, value: Response<A>) {
        val surrogate = when (value) {
            is Response.Success -> ResponseSurrogate(
                ResponseSurrogate.Status.Success,
                timestamp = value.timestamp,
                data = value.data
            )

            is Response.Failure -> ResponseSurrogate(
                ResponseSurrogate.Status.Failure,
                timestamp = value.timestamp,
                errors = value.errors
            )
        }
        surrogateSerializer.serialize(encoder, surrogate)
    }
}
