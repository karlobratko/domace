package hr.algebra.domace.infrastructure.persistence.exposed

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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
