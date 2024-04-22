package hr.algebra.domace.domain.persistence

import arrow.core.Either
import arrow.core.Option
import hr.algebra.domace.domain.DomainError
import hr.algebra.domace.domain.model.User

interface UserPersistence {
    suspend fun insert(user: User.New): Either<DomainError, User.Entity>

    suspend fun select(username: User.Username): Option<User.Entity>

    suspend fun select(id: User.Id): Option<User.Entity>

    suspend fun select(username: User.Username, password: User.Password): Option<User.Entity>

    suspend fun update(data: User.Edit): Either<DomainError, User.Entity>

    suspend fun update(data: User.ChangePassword): Either<DomainError, User.Entity>

    suspend fun delete(id: User.Id): Either<DomainError, User.Id>
}
