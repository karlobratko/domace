package hr.algebra.domace.domain.mailing

/**
 * Represents an Email Template.
 *
 * This is a functional interface, meaning it has a single abstract method. In this case, the method is `email`.
 * The `email` method is used to convert the `EmailTemplate` object into an `Email` object.
 */
fun interface EmailTemplate {
    /**
     * Converts the `EmailTemplate` object into an `Email` object.
     *
     * @return The `Email` object.
     */
    fun email(): Email
}
