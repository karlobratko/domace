package hr.algebra.domace.infrastructure.mailing

import hr.algebra.domace.domain.mailing.Email.Address
import hr.algebra.domace.domain.mailing.Email.Participant
import hr.algebra.domace.domain.mailing.Email.Participant.Name
import hr.algebra.domace.infrastructure.mailing.jakarta.JakartaMailingService
import hr.algebra.domace.infrastructure.serialization.Resources
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import java.util.Properties
import org.koin.dsl.module

val MailingModule =
    module {
        single {
            val config: MailingConfig = Resources.hocon("mail/mail.dev.conf")
            val props = Properties().also {
                it["mail.smtp.auth"] = config.smtp.auth
                it["mail.smtp.starttls.enable"] = config.smtp.startTls
                it["mail.smtp.host"] = config.smtp.host
                it["mail.smtp.port"] = config.smtp.port
                it["mail.smtp.ssl.trust"] = config.smtp.ssl.trust
            }

            val secrets: MailingCredentialsConfig = Resources.hocon("secrets/mail.dev.conf")
            Session.getInstance(
                props,
                object : Authenticator() {
                    override fun getPasswordAuthentication() =
                        PasswordAuthentication(secrets.username, secrets.password)
                }
            )
        }

        single(createdAtStart = true) {
            val config: MailingConfig = Resources.hocon("mail/mail.dev.conf")

            Senders(
                info = Participant(
                    address = Address(config.senders.info.address),
                    name = Name(config.senders.info.name)
                ),
                auth = Participant(
                    address = Address(config.senders.auth.address),
                    name = Name(config.senders.auth.name)
                ),
                noReply = Participant(
                    address = Address(config.senders.noReply.address),
                    name = Name(config.senders.noReply.name)
                ),
            )
        }

        single { JakartaMailingService(get()) }
    }
