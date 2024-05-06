package hr.algebra.domace.infrastructure.mailing.templates

import arrow.core.nel
import hr.algebra.domace.domain.mailing.Email
import hr.algebra.domace.domain.mailing.Email.Participant
import hr.algebra.domace.domain.mailing.EmailTemplate
import hr.algebra.domace.domain.model.User
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.stream.createHTML

fun ConfirmRegistration(
    from: Participant,
    email: User.Email,
    username: User.Username,
    confirmUrl: String
) = EmailTemplate {
    Email(
        from = from,
        to = Participant(
            address = Email.Address(email.value),
            name = Participant.Name(username.value)
        ).nel(),
        subject = Email.Subject("Confirm Registration"),
        content = Email.Content.Html(
            value = createHTML()
                .html {
                    head {
                        meta {
                            name = "viewport"
                            content = "width=device-width, initial-scale=1.0"
                        }
                        meta {
                            httpEquiv = "Content-Type"
                            content = "text/html; charset=UTF-8"
                        }
                    }
                    body {
                        div {
                            h1 { +"Hello ${username.value}!" }
                            p { +"It is nice to have you here. :)" }
                            p {
                                +"Confirm your registration on this "
                                a(href = confirmUrl) {
                                    +"link"
                                }
                                +"."
                            }
                        }
                    }
                }
        )
    )
}
