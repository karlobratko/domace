package hr.algebra.domace.infrastructure.security

import hr.algebra.domace.domain.conversion.ConversionScope
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.Claims

typealias UserIdToSubjectConversionScope = ConversionScope<User.Id, Claims.Subject>

val UserIdToSubjectConversion = UserIdToSubjectConversionScope { Claims.Subject(value.toString()) }

typealias SubjectToUserIdConversionScope = ConversionScope<Claims.Subject, User.Id>

val SubjectToUserIdConversion = SubjectToUserIdConversionScope { User.Id(value.toLong()) }
