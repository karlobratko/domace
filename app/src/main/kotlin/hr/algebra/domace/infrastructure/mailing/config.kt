package hr.algebra.domace.infrastructure.mailing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class MailingConfig(val smtp: SmtpConfig, val senders: SendersConfig) {
    @Serializable data class SmtpConfig(
        val auth: Boolean,
        @SerialName("start-tls") val startTls: Boolean,
        val host: String,
        val port: Int,
        val ssl: SslConfig
    ) {
        @Serializable data class SslConfig(val trust: String)
    }

    @Serializable data class SendersConfig(
        val info: ParticipantConfig,
        val auth: ParticipantConfig,
        @SerialName("no-reply") val noReply: ParticipantConfig
    ) {
        @Serializable data class ParticipantConfig(val address: String, val name: String)
    }
}

@Serializable data class MailingCredentialsConfig(val username: String, val password: String)
