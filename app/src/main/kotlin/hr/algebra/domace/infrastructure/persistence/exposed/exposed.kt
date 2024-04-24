package hr.algebra.domace.infrastructure.persistence.exposed

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Executes a given block of code within a new suspended transaction on the IO dispatcher.
 *
 * This function is used to perform database operations on the IO dispatcher, which is designed for offloading blocking
 * IO tasks to shared pool of threads.
 *
 * @param db The Database instance on which the transaction is to be performed.
 * @param block The block of code to be executed within the transaction. This is a suspending function that can be used
 * to perform suspending database operations.
 *
 * @return The result of the block of code executed within the transaction.
 *
 * @throws IllegalStateException If this transaction is already completed (commit or rollback is called).
 */
suspend fun <T> ioTransaction(
    db: Database,
    block: suspend () -> T
): T = newSuspendedTransaction(Dispatchers.IO, db) { block() }

inline fun <reified T : Enum<T>> enumToSql() =
    enumValues<T>().joinToString(
        separator = "', '",
        prefix = "ENUM('",
        postfix = "')"
    )

inline fun <reified T : Enum<T>> Transaction.createEnumeration(name: String = "") {
    val enumName: String =
        when {
            name.isNotEmpty() -> name
            javaClass.`package` == null -> javaClass.name.removeSuffix("Enum")
            else -> javaClass.name.removePrefix("${javaClass.`package`.name}.").substringAfter('$').removeSuffix("Enum")
        }
    exec("CREATE TYPE $enumName AS ${enumToSql<T>()}")
}

inline fun <reified T : Enum<T>> Table.customEnumeration(
    name: String,
    noinline fromDb: (Any) -> T,
    noinline toDb: (T) -> Any
) = customEnumeration(name, enumToSql<T>(), fromDb, toDb)

inline fun <reified T : Enum<T>> Table.customEnumeration(name: String) =
    customEnumeration<T>(name, { enumValues<T>()[it as Int] }, { it.ordinal })

inline fun <reified T : Enum<T>> Table.customEnumerationByName(name: String) =
    customEnumeration<T>(name, { enumValueOf<T>(it as String) }, { it.name })
