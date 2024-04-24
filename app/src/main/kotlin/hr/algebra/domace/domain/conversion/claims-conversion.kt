package hr.algebra.domace.domain.conversion

import hr.algebra.domace.domain.ConversionError.ValidationNotPerformed
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.Claims
import hr.algebra.domace.domain.toLongOrLeft

typealias UserIdToSubjectConversionScope = ConversionScope<User.Id, Claims.Subject>

val UserIdToSubjectConversion = UserIdToSubjectConversionScope { Claims.Subject(value.toString()) }

typealias SubjectToUserIdConversionScope = FailingConversionScope<ValidationNotPerformed, Claims.Subject, User.Id>

val SubjectToUserIdConversion = SubjectToUserIdConversionScope {
    value.toLongOrLeft { ValidationNotPerformed }.map { User.Id(it) }
}
