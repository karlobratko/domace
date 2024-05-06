package hr.algebra.domace.infrastructure.persistence.exposed

import arrow.core.None
import arrow.core.Some
import arrow.core.toOption
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.withSuspendTransaction
import kotlin.coroutines.CoroutineContext

/**
 * Executes a block of code within a transaction.
 *
 * This function is a suspending function, meaning it is designed to be used with Kotlin's coroutines.
 * It executes a block of code within a transaction, either using the current transaction if one exists,
 * or creating a new transaction if one does not.
 *
 * @param context The CoroutineContext to use for the transaction. Defaults to Dispatchers.IO.
 * @param db The Database to use for the transaction.
 * @param block The block of code to execute within the transaction.
 *
 * @return The result of the block of code.
 */
suspend fun <T> ioTransaction(
    context: CoroutineContext = Dispatchers.IO,
    db: Database,
    block: suspend Transaction.() -> T
): T = when (val transaction = TransactionManager.currentOrNone()) {
    is Some -> transaction.value.withSuspendTransaction(context = context) { block() }
    is None -> newSuspendedTransaction(context = context, db = db) { block() }
}

/**
 * Returns the current transaction if one exists, or None if one does not.
 *
 * @return An Option containing the current transaction if one exists, or None if one does not.
 */
fun TransactionManager.Companion.currentOrNone() = currentOrNull().toOption()

/**
 * Converts an enumeration to a SQL ENUM type.
 *
 * @return A string representing the SQL ENUM type.
 */
inline fun <reified T : Enum<T>> enumToSql() =
    enumValues<T>().joinToString(
        separator = "', '",
        prefix = "ENUM('",
        postfix = "')"
    )

/**
 * Creates a SQL ENUM type for an enumeration.
 *
 * @param name The name of the SQL ENUM type. If not provided, the name of the enumeration is used.
 */
inline fun <reified T : Enum<T>> Transaction.createEnumeration(name: String = "") {
    val enumName: String =
        when {
            name.isNotEmpty() -> name
            javaClass.`package` == null -> javaClass.name.removeSuffix("Enum")
            else -> javaClass.name.removePrefix("${javaClass.`package`.name}.").substringAfter('$').removeSuffix("Enum")
        }
    exec("CREATE TYPE $enumName AS ${enumToSql<T>()}")
}

/**
 * Creates a custom enumeration column in a table.
 *
 * @param name The name of the column.
 * @param fromDb A function to convert from the database value to the enumeration.
 * @param toDb A function to convert from the enumeration to the database value.
 *
 * @return The created column.
 */
inline fun <reified T : Enum<T>> Table.customEnumeration(
    name: String,
    noinline fromDb: (Any) -> T,
    noinline toDb: (T) -> Any
) = customEnumeration(name, enumToSql<T>(), fromDb, toDb)

/**
 * Creates a custom enumeration column in a table, using the ordinal of the enumeration.
 *
 * @param name The name of the column.
 *
 * @return The created column.
 */
inline fun <reified T : Enum<T>> Table.customEnumeration(name: String) =
    customEnumeration<T>(name, { enumValues<T>()[it as Int] }, { it.ordinal })

/**
 * Creates a custom enumeration column in a table, using the name of the enumeration.
 *
 * @param name The name of the column.
 *
 * @return The created column.
 */
inline fun <reified T : Enum<T>> Table.customEnumerationByName(name: String) =
    customEnumeration<T>(name, { enumValueOf<T>(it as String) }, { it.name })
