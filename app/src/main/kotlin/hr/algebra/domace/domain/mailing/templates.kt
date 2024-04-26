package hr.algebra.domace.domain.mailing

import arrow.core.nel
import hr.algebra.domace.domain.mailing.Email.Content.Html
import hr.algebra.domace.domain.mailing.Email.Participant
import hr.algebra.domace.domain.mailing.Email.Subject
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.stream.createHTML

fun interface EmailTemplate {
    fun email(): Email
}

fun HelloEmailTemplate(from: Participant, to: Participant) = EmailTemplate {
    Email(
        from = from,
        to = to.nel(),
        subject = Subject("Test Mail"),
        content = Html(
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
                            h1 { +"Hello ${to.name.value}!" }
                            p { +"It is nice to have you here. :)" }
                        }
                    }
                }
        )
    )
}
