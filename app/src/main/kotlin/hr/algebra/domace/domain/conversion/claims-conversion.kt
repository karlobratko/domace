package hr.algebra.domace.domain.conversion

import arrow.core.raise.catch
import arrow.core.raise.either
import hr.algebra.domace.domain.ConversionError.ValidationNotPerformed
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.security.jwt.Claims
import hr.algebra.domace.domain.toLongOrLeft

typealias UserIdToSubjectConversionScope = ConversionScope<User.Id, Claims.Subject>

val UserIdToSubjectConversion = UserIdToSubjectConversionScope { Claims.Subject(value.toString()) }

typealias SubjectToUserIdConversionScope = FailingConversionScope<ValidationNotPerformed, Claims.Subject, User.Id>

val SubjectToUserIdConversion = SubjectToUserIdConversionScope {
    value.toLongOrLeft { ValidationNotPerformed }.map { User.Id(it) }
}

typealias RoleToRoleClaimConversionScope = ConversionScope<User.Role, Claims.Role>

val RoleToRoleClaimConversion = RoleToRoleClaimConversionScope { Claims.Role(name) }

typealias RoleClaimToRoleConversionScope = FailingConversionScope<ValidationNotPerformed, Claims.Role, User.Role>

val RoleClaimToRoleConversion = RoleClaimToRoleConversionScope {
    either {
        catch({
            User.Role.valueOf(value)
        }) { raise(ValidationNotPerformed) }
    }
}
