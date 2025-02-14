package hr.algebra.domace.domain.conversion

import arrow.core.Either

/**
 * A functional interface that represents a conversion operation from type A to type B.
 *
 * This interface is used to define a conversion scope where an object of type A can be converted to an object
 * of type B.
 * The conversion logic is defined in the `convert` function.
 *
 * @param A The type of the object to be converted.
 * @param B The type of the object after conversion.
 */
fun interface ConversionScope<A, B> {
    /**
     * Converts an object of type A to an object of type B.
     *
     * @receiver The object of type A to be converted.
     * @return The object of type B after conversion.
     */
    fun A.convert(): B
}

/**
 * A typealias for ConversionScope that represents a conversion operation from type A to type B,
 * which may fail with an error of type Error.
 *
 * This typealias is used to define a conversion scope where an object of type A can be converted to an object
 * of type B or an error of type Error.
 * The conversion logic is defined in the `convert` function of the ConversionScope.
 *
 * @param Error The type of the error that may occur during conversion.
 * @param A The type of the object to be converted.
 * @param B The type of the object after successful conversion.
 */
typealias FailingConversionScope<Error, A, B> = ConversionScope<A, Either<Error, B>>

/**
 * Converts an object of type A to an object of type B using the provided ConversionScope.
 *
 * This function is an extension function on the type A, and it uses the `convert` function of the provided
 * ConversionScope to perform the conversion.
 *
 * @param scope The ConversionScope to use for the conversion. It must be a ConversionScope<A, B>, where A is the type
 * of the receiver and B is the type of the result.
 *
 * @return The converted object of type B.
 */
fun <A, B> A.convert(scope: ConversionScope<A, B>): B = with(scope) { this@convert.convert() }
