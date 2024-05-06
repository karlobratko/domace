package hr.algebra.domace.domain.security

/**
 * Represents a Token.
 *
 * This is a sealed interface, meaning it can only be implemented by classes in the same file.
 * It contains several subtypes, each representing a different kind of token.
 *
 * @property value The value of the token.
 */
sealed interface Token {

    val value: String

    /**
     * Represents a Refresh Token.
     *
     * This is a value class, meaning it only contains the value of the refresh token.
     * It is also a subtype of the Token interface.
     *
     * @property value The value of the refresh token.
     */
    @JvmInline value class Refresh(override val value: String) : Token

    /**
     * Represents an Access Token.
     *
     * This is a value class, meaning it only contains the value of the access token.
     * It is also a subtype of the Token interface.
     *
     * @property value The value of the access token.
     */
    @JvmInline value class Access(override val value: String) : Token

    /**
     * Represents a Register Token.
     *
     * This is a value class, meaning it only contains the value of the register token.
     * It is also a subtype of the Token interface.
     *
     * @property value The value of the register token.
     */
    @JvmInline value class Register(override val value: String) : Token
}
