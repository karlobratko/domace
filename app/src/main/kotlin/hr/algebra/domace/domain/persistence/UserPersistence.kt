package hr.algebra.domace.domain.persistence

import arrow.core.Either
import arrow.core.Option
import hr.algebra.domace.domain.DbError.InvalidUsernameOrPassword
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.model.User
import hr.algebra.domace.domain.model.User.Password
import hr.algebra.domace.domain.model.User.Username

/**
 * Interface for the persistence operations related to Users.
 */
interface UserPersistence {
    /**
     * Inserts a new user into the persistence layer.
     *
     * @param user The new user to be inserted.
     * @return Either a DomainError or the inserted User Entity.
     */
    suspend fun insert(user: User.New): Either<DomainError, User>

    /**
     * Selects a user from the persistence layer based on their username.
     *
     * @param username The username of the user to be selected.
     * @return An Option of User Entity.
     */
    suspend fun select(username: Username): Option<User>

    /**
     * Selects a user from the persistence layer based on their ID.
     *
     * @param id The ID of the user to be selected.
     * @return An Option of User Entity.
     */
    suspend fun select(id: User.Id): Option<User>

    /**
     * Selects a user from the persistence layer based on their username and password.
     *
     * @param username The username of the user to be selected.
     * @param password The password of the user to be selected.
     * @return Either an InvalidUsernameOrPassword error or the User entity joined with RegistrationToken.
     */
    suspend fun select(username: Username, password: Password): Either<InvalidUsernameOrPassword, User.WithToken>

    /**
     * Updates a user in the persistence layer.
     *
     * @param data The data to be updated for the user.
     * @return Either a DomainError or the updated User Entity.
     */
    suspend fun update(data: User.Edit): Either<DomainError, User>

    /**
     * Updates the password of a user in the persistence layer.
     *
     * @param data The data to be updated for the user's password.
     * @return Either a DomainError or the updated User Entity.
     */
    suspend fun update(data: User.ChangePassword): Either<DomainError, User>

    /**
     * Deletes a user from the persistence layer based on their ID.
     *
     * @param id The ID of the user to be deleted.
     * @return Either a DomainError or the ID of the deleted user.
     */
    suspend fun delete(id: User.Id): Either<DomainError, User.Id>
}
