package hr.algebra.domace.infrastructure.persistence.exposed

import hr.algebra.domace.domain.DbError
import hr.algebra.domace.domain.conversion.ConversionScope
import org.jetbrains.exposed.exceptions.ExposedSQLException

typealias ExposedSQLExceptionToDomainErrorConversionScope = ConversionScope<ExposedSQLException, DbError>
