package hr.algebra.domace.infrastructure.mailing

import hr.algebra.domace.domain.mailing.Email.Participant

data class Senders(
    val info: Participant,
    val auth: Participant,
    val noReply: Participant
)
