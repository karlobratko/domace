package hr.algebra.domace.domain.validation

import arrow.core.EitherNel
import arrow.core.left
import arrow.core.raise.ensure
import arrow.core.right
import arrow.core.toNonEmptyListOrNone
import hr.algebra.domace.domain.applyWrapEitherNel
import java.math.BigDecimal

/**
 * A functional interface for a validation scope.
 *
 * This interface defines a single function, `validate`, which is used to validate an object of type A.
 * The function returns an `EitherNel` object. If the validation is successful, the `EitherNel` object will
 * contain the object of type A.
 * If the validation fails, the `EitherNel` object will contain an error of type Error.
 *
 * @param Error The type of the error that may occur during validation.
 * @param A The type of the object to be validated.
 */
fun interface ValidationScope<Error, A> {
    /**
     * Validates an object of type A.
     *
     * @receiver The object of type A to be validated.
     * @return An `EitherNel` object containing a `Error` object if the validation fails, or the object of type A if
     * the validation is successful.
     */
    fun A.validate(): EitherNel<Error, A>
}

/**
 * Validates a given value using the provided validation scope.
 *
 * This function takes a value of type A and a validation scope. It applies the validation scope to the value
 * and returns the result as an `EitherNel` object. If the validation is successful, the `EitherNel` object will
 * contain the value of type A.
 * If the validation fails, the `EitherNel` object will contain an error of type Error.
 *
 * @param value The value of type A to be validated.
 * @param scope The validation scope to be used for validation.
 *
 * @return An `EitherNel` object containing a `Error` object if the validation fails, or the value of type A if the
 * validation is successful.
 */
fun <Error, A> validate(value: A, scope: ValidationScope<Error, A>): EitherNel<Error, A> =
    with(scope) { value.validate() }

/**
 * A class for validating a given value.
 *
 * This class takes a value of type A and provides methods to apply validation scopes or validation functions to it.
 * The results of the validations are collected in the `errors` list.
 *
 * @param Error The type of the error that may occur during validation.
 * @param A The type of the value to be validated.
 * @property value The value of type A to be validated.
 * @property errors A mutable list of errors of type Error that occurred during validation.
 */
class Validator<Error, A>(private val value: A) {
    val errors: MutableList<Error> = mutableListOf()

    /**
     * Applies a validation scope to the value.
     *
     * This function takes a validation scope and applies it to the value. If the validation fails, the error is added
     * to the `errors` list.
     *
     * @param scope The validation scope to be used for validation.
     */
    infix fun with(scope: ValidationScope<Error, A>) {
        validate(value, scope).onLeft {
            errors.addAll(it)
        }
    }

    /**
     * Applies a validation function to the value.
     *
     * This function takes a validation function and applies it to the value. If the validation fails, the error is
     * added to the `errors` list.
     *
     * @param block The validation function to be used for validation.
     */
    infix fun <B> with(block: (A) -> EitherNel<Error, B>) {
        block(value).onLeft {
            errors.addAll(it)
        }
    }
}

/**
 * Creates a new Validator instance for a given value.
 *
 * This function takes a value of type A and creates a new Validator instance for it. The Validator can then be used
 * to apply validation scopes or validation functions to the value.
 *
 * @param value The value of type A to be validated.
 *
 * @return A new Validator instance for the given value.
 */
fun <Error, A> validate(value: A): Validator<Error, A> = Validator(value)

/**
 * Validates a given value using the provided validation block.
 *
 * This function takes a value of type A and a validation block. It creates a new Validator instance for the value,
 * applies the validation block to the Validator, and returns the result as an `EitherNel` object.
 * If the validation is successful, the `EitherNel` object will contain the value of type A.
 * If the validation fails, the `EitherNel` object will contain an error of type Error.
 *
 * @param value The value of type A to be validated.
 * @param block The validation block to be used for validation. This block is a function that is applied to the
 * Validator instance.
 *
 * @return An `EitherNel` object containing a `Error` object if the validation fails, or the value of type A if the
 * validation is successful.
 */
fun <Error, A> validate(value: A, block: Validator<Error, A>.() -> Unit): EitherNel<Error, A> =
    Validator<Error, A>(value).run {
        block()
        errors.toNonEmptyListOrNone()
            .fold({ value.right() }, { it.left() })
    }

/**
 * Validates a given value using the provided validation block and property.
 *
 * This function takes a value of type A, a property function that transforms the value to type B, and a
 * validation block.
 * It creates a new Validator instance for the transformed value, applies the validation block to the Validator,
 * and returns the result as an `EitherNel` object.
 * If the validation is successful, the `EitherNel` object will contain the original value of type A.
 * If the validation fails, the `EitherNel` object will contain an error of type Error.
 *
 * @param value The value of type A to be validated.
 * @param property The function that transforms the value of type A to type B.
 * @param block The validation block to be used for validation. This block is a function that is applied to the
 * Validator instance.
 *
 * @return An `EitherNel` object containing a `Error` object if the validation fails, or the original value of type A
 * if the validation is successful.
 */
fun <Error, A, B> validate(value: A, property: (A) -> B, block: Validator<Error, B>.() -> Unit): EitherNel<Error, A> =
    Validator<Error, B>(property(value)).run {
        block()
        errors.toNonEmptyListOrNone()
            .fold({ value.right() }, { it.left() })
    }

/**
 * Type alias for a ValidationScope that validates a String.
 *
 * This type alias simplifies the use of ValidationScope for String validation. It defines a ValidationScope where
 * the object to be validated is of type String.
 *
 * @param Error The type of the error that may occur during validation.
 */
typealias StringValidationScope<Error> = ValidationScope<Error, String>

/**
 * Creates a StringValidationScope that validates if a string's length is greater than or equal to a minimum length.
 *
 * This function takes a minimum length and an error function. It creates a new StringValidationScope that validates
 * if a string's length is greater than or equal to the minimum length. If the validation fails, the error function is
 * called to generate an error of type Error.
 *
 * @param min The minimum length that the string should have.
 * @param error A function that generates an error of type Error.
 *
 * @return A StringValidationScope that validates if a string's length is greater than or equal to the minimum length.
 */
fun <Error> StringMinLengthValidation(min: Int, error: () -> Error) = StringValidationScope {
    applyWrapEitherNel {
        ensure(length >= min, error)
    }
}

/**
 * Creates a StringValidationScope that validates if a string's length is less than or equal to a maximum length.
 *
 * This function takes a maximum length and an error function. It creates a new StringValidationScope that validates
 * if a string's length is less than or equal to the maximum length. If the validation fails, the error function is
 * called to generate an error of type Error.
 *
 * @param max The maximum length that the string should have.
 * @param error A function that generates an error of type Error.
 *
 * @return A StringValidationScope that validates if a string's length is less than or equal to the maximum length.
 */
fun <Error> StringMaxLengthValidation(max: Int, error: () -> Error) = StringValidationScope {
    applyWrapEitherNel {
        ensure(length <= max, error)
    }
}

/**
 * Creates a StringValidationScope that validates if a string matches a given regular expression.
 *
 * This function takes a regular expression and an error function. It creates a new StringValidationScope that validates
 * if a string matches the regular expression. If the validation fails, the error function is called to generate an
 * error of type Error.
 *
 * @param regex The regular expression that the string should match.
 * @param error A function that generates an error of type Error.
 *
 * @return A StringValidationScope that validates if a string matches the given regular expression.
 */
fun <Error> StringMatchingPatternValidation(regex: Regex, error: () -> Error) = StringValidationScope {
    applyWrapEitherNel {
        ensure(matches(regex), error)
    }
}

/**
 * Creates a StringValidationScope that validates if none of the characters in a string satisfy a given predicate.
 *
 * This function takes a predicate function and an error function. It creates a new StringValidationScope that validates
 * if none of the characters in a string satisfy the predicate. If the validation fails, the error function is called
 * to generate an error of type Error.
 *
 * @param predicate The predicate function that should be satisfied by none of the characters in the string.
 * @param error A function that generates an error of type Error.
 *
 * @return A StringValidationScope that validates if none of the characters in a string satisfy the given predicate.
 */
fun <Error> StringNoneSatisfiesPredicateValidation(predicate: (Char) -> Boolean, error: () -> Error) =
    StringValidationScope {
        applyWrapEitherNel {
            ensure(none(predicate), error)
        }
    }

/**
 * Creates a StringValidationScope that validates if any of the characters in a string satisfy a given predicate.
 *
 * This function takes a predicate function and an error function. It creates a new StringValidationScope that validates
 * if any of the characters in a string satisfy the predicate. If the validation fails, the error function is called
 * to generate an error of type Error.
 *
 * @param predicate The predicate function that should be satisfied by any of the characters in the string.
 * @param error A function that generates an error of type Error.
 *
 * @return A StringValidationScope that validates if any of the characters in a string satisfy the given predicate.
 */
fun <Error> StringAnySatisfiesPredicateValidation(predicate: (Char) -> Boolean, error: () -> Error) =
    StringValidationScope {
        applyWrapEitherNel {
            ensure(any(predicate), error)
        }
    }

/**
 * Creates a StringValidationScope that validates if all of the characters in a string satisfy a given predicate.
 *
 * This function takes a predicate function and an error function. It creates a new StringValidationScope that validates
 * if all of the characters in a string satisfy the predicate. If the validation fails, the error function is called
 * to generate an error of type Error.
 *
 * @param predicate The predicate function that should be satisfied by all of the characters in the string.
 * @param error A function that generates an error of type Error.
 *
 * @return A StringValidationScope that validates if all of the characters in a string satisfy the given predicate.
 */
fun <Error> StringAllSatisfiesPredicateValidation(predicate: (Char) -> Boolean, error: () -> Error) =
    StringValidationScope {
        applyWrapEitherNel {
            ensure(all(predicate), error)
        }
    }

/**
 * Type alias for a ValidationScope that validates an Int.
 *
 * This type alias simplifies the use of ValidationScope for Int validation. It defines a ValidationScope where
 * the object to be validated is of type Int.
 *
 * @param Error The type of the error that may occur during validation.
 */
typealias IntValidationScope<Error> = ValidationScope<Error, Int>

/**
 * Creates an IntValidationScope that validates if an integer is within a given range.
 *
 * This function takes a range and an error function. It creates a new IntValidationScope that validates
 * if an integer is within the range. If the validation fails, the error function is called to generate an error of
 * type Error.
 *
 * @param range The range that the integer should be within.
 * @param error A function that generates an error of type Error.
 *
 * @return An IntValidationScope that validates if an integer is within the given range.
 */
fun <Error> IntInRangeValidation(range: IntRange, error: () -> Error) = IntValidationScope validation@{
    applyWrapEitherNel {
        ensure(this@validation in range) { error() }
    }
}

/**
 * Creates an IntValidationScope that validates if an integer is within a given inclusive range.
 *
 * This function takes a minimum and maximum value and an error function. It creates a new IntValidationScope that
 * validates if an integer is within the inclusive range from the minimum to the maximum value. If the validation
 * fails, the error function is called to generate an error of type Error.
 *
 * @param min The minimum value that the integer should be.
 * @param max The maximum value that the integer should be.
 * @param error A function that generates an error of type Error.
 *
 * @return An IntValidationScope that validates if an integer is within the given inclusive range.
 */
fun <Error> IntInInclusiveRangeValidation(min: Int, max: Int, error: () -> Error) =
    IntInRangeValidation(min..max, error)

/**
 * Creates an IntValidationScope that validates if an integer is within a given exclusive range.
 *
 * This function takes a minimum and maximum value and an error function. It creates a new IntValidationScope that
 * validates if an integer is within the exclusive range from the minimum to the maximum value. If the validation
 * fails, the error function is called to generate an error of type Error.
 *
 * @param min The minimum value that the integer should be.
 * @param maxExclusive The maximum value that the integer should be, exclusive.
 * @param error A function that generates an error of type Error.
 *
 * @return An IntValidationScope that validates if an integer is within the given exclusive range.
 */
fun <Error> IntInExclusiveRangeValidation(min: Int, maxExclusive: Int, error: () -> Error) =
    IntInRangeValidation(min..<maxExclusive, error)

/**
 * Type alias for a ValidationScope that validates a BigDecimal.
 *
 * This type alias simplifies the use of ValidationScope for BigDecimal validation. It defines a ValidationScope where
 * the object to be validated is of type BigDecimal.
 *
 * @param Error The type of the error that may occur during validation.
 */
typealias BigDecimalValidationScope<Error> = ValidationScope<Error, BigDecimal>

/**
 * Creates a BigDecimalValidationScope that validates if a BigDecimal is less than a given maximum value.
 *
 * This function takes a maximum value and an error function. It creates a new BigDecimalValidationScope that validates
 * if a BigDecimal is less than the maximum value. If the validation fails, the error function is called to generate
 * an error of type Error.
 *
 * @param max The maximum value that the BigDecimal should be less than.
 * @param error A function that generates an error of type Error.
 *
 * @return A BigDecimalValidationScope that validates if a BigDecimal is less than the given maximum value.
 */
fun <Error> BigDecimalLessThanValidation(max: BigDecimal, error: () -> Error) = BigDecimalValidationScope validation@{
    applyWrapEitherNel {
        ensure(this@validation < max) { error() }
    }
}

/**
 * Creates a BigDecimalValidationScope that validates if a BigDecimal is less than or equal to a given maximum value.
 *
 * This function takes a maximum value and an error function. It creates a new BigDecimalValidationScope that validates
 * if a BigDecimal is less than or equal to the maximum value. If the validation fails, the error function is called
 * to generate an error of type Error.
 *
 * @param max The maximum value that the BigDecimal should be less than or equal to.
 * @param error A function that generates an error of type Error.
 *
 * @return A BigDecimalValidationScope that validates if a BigDecimal is less than or equal to the given maximum value.
 */
fun <Error> BigDecimalLessThanOrEqualValidation(max: BigDecimal, error: () -> Error) =
    BigDecimalValidationScope validation@{
        applyWrapEitherNel {
            ensure(this@validation <= max) { error() }
        }
    }

/**
 * Creates a BigDecimalValidationScope that validates if a BigDecimal is greater than a given minimum value.
 *
 * This function takes a minimum value and an error function. It creates a new BigDecimalValidationScope that validates
 * if a BigDecimal is greater than the minimum value. If the validation fails, the error function is called to
 * generate an error of type Error.
 *
 * @param min The minimum value that the BigDecimal should be greater than.
 * @param error A function that generates an error of type Error.
 *
 * @return A BigDecimalValidationScope that validates if a BigDecimal is greater than the given minimum value.
 */
fun <Error> BigDecimalGreaterThanValidation(min: BigDecimal, error: () -> Error) =
    BigDecimalValidationScope validation@{
        applyWrapEitherNel {
            ensure(this@validation > min) { error() }
        }
    }

/**
 * Creates a BigDecimalValidationScope that validates if a BigDecimal is greater than or equal to a given minimum value.
 *
 * This function takes a minimum value and an error function. It creates a new BigDecimalValidationScope that validates
 * if a BigDecimal is greater than or equal to the minimum value. If the validation fails, the error function is
 * called to generate an error of type Error.
 *
 * @param min The minimum value that the BigDecimal should be greater than or equal to.
 * @param error A function that generates an error of type Error.
 *
 * @return A BigDecimalValidationScope that validates if a BigDecimal is greater than or equal to the given
 * minimum value.
 */
fun <Error> BigDecimalGreaterThanOrEqualValidation(min: BigDecimal, error: () -> Error) =
    BigDecimalValidationScope validation@{
        applyWrapEitherNel {
            ensure(this@validation >= min) { error() }
        }
    }

/**
 * Type alias for a ValidationScope that validates a Short.
 *
 * This type alias simplifies the use of ValidationScope for Short validation. It defines a ValidationScope where
 * the object to be validated is of type Short.
 *
 * @param Error The type of the error that may occur during validation.
 */
typealias ShortValidationScope<Error> = ValidationScope<Error, Short>

/**
 * Creates a ShortValidationScope that validates if a Short is less than a given maximum value.
 *
 * This function takes a maximum value and an error function. It creates a new ShortValidationScope that validates
 * if a Short is less than the maximum value. If the validation fails, the error function is called to generate
 * an error of type Error.
 *
 * @param max The maximum value that the Short should be less than.
 * @param error A function that generates an error of type Error.
 *
 * @return A ShortValidationScope that validates if a Short is less than the given maximum value.
 */
fun <Error> ShortLessThanValidation(max: Short, error: () -> Error) = ShortValidationScope validation@{
    applyWrapEitherNel {
        ensure(this@validation < max) { error() }
    }
}

/**
 * Creates a ShortValidationScope that validates if a Short is less than or equal to a given maximum value.
 *
 * This function takes a maximum value and an error function. It creates a new ShortValidationScope that validates
 * if a Short is less than or equal to the maximum value. If the validation fails, the error function is called to
 * generate an error of type Error.
 *
 * @param max The maximum value that the Short should be less than or equal to.
 * @param error A function that generates an error of type Error.
 *
 * @return A ShortValidationScope that validates if a Short is less than or equal to the given maximum value.
 */
fun <Error> ShortLessThanOrEqualValidation(max: Short, error: () -> Error) = ShortValidationScope validation@{
    applyWrapEitherNel {
        ensure(this@validation <= max) { error() }
    }
}

/**
 * Creates a ShortValidationScope that validates if a Short is greater than a given minimum value.
 *
 * This function takes a minimum value and an error function. It creates a new ShortValidationScope that validates
 * if a Short is greater than the minimum value. If the validation fails, the error function is called to generate
 * an error of type Error.
 *
 * @param min The minimum value that the Short should be greater than.
 * @param error A function that generates an error of type Error.
 *
 * @return A ShortValidationScope that validates if a Short is greater than the given minimum value.
 */
fun <Error> ShortGreaterThanValidation(min: Short, error: () -> Error) = ShortValidationScope validation@{
    applyWrapEitherNel {
        ensure(this@validation > min) { error() }
    }
}

/**
 * Creates a ShortValidationScope that validates if a Short is greater than or equal to a given minimum value.
 *
 * This function takes a minimum value and an error function. It creates a new ShortValidationScope that validates
 * if a Short is greater than or equal to the minimum value. If the validation fails, the error function is called
 * to generate an error of type Error.
 *
 * @param min The minimum value that the Short should be greater than or equal to.
 * @param error A function that generates an error of type Error.
 *
 * @return A ShortValidationScope that validates if a Short is greater than or equal to the given minimum value.
 */
fun <Error> ShortGreaterThanOrEqualValidation(min: Short, error: () -> Error) = ShortValidationScope validation@{
    applyWrapEitherNel {
        ensure(this@validation >= min) { error() }
    }
}
