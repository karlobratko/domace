package hr.algebra.domace.domain.conversion

import arrow.core.Either

fun interface ConversionScope<A, B> {
    fun A.convert(): B
}

typealias FailingConversionScope<Error, A, B> = ConversionScope<A, Either<Error, B>>
