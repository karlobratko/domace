package hr.algebra.domace.infrastructure.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import hr.algebra.domace.infrastructure.serialization.Resources
import kotlinx.serialization.Serializable
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Database.Companion.connect
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.StdOutSqlLogger
import java.sql.Connection.TRANSACTION_SERIALIZABLE as TransactionSerializable

object Database {
    val h2 by lazy {
        val dataSource =
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = "jdbc:h2:mem:development"
                    driverClassName = "org.h2.Driver"
                    username = "username"
                    password = "password"
                    maximumPoolSize = 6
                    isReadOnly = true
                }
            )

        migrateFlyway(dataSource)

        connectToDatabase(dataSource)
    }

    val postgres by lazy {
        @Serializable
        data class DataSourceProperties(
            val driverClass: String,
            val driver: String,
            val database: String,
            val user: String,
            val password: String
        )

        @Serializable
        data class DbProperties(
            val jdbcUrl: String,
            val dataSource: DataSourceProperties,
            val maximumPoolSize: Int
        )

        val props: DbProperties = Resources.properties("db/config/db.properties")
        val dataSource =
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = props.jdbcUrl
                    props.dataSource.also {
                        driverClassName = it.driverClass
                        username = it.user
                        password = it.password
                    }
                    maximumPoolSize = props.maximumPoolSize
                }
            )

        migrateFlyway(dataSource)

        connectToDatabase(dataSource)
    }

    private fun migrateFlyway(dataSource: HikariDataSource) {
        val flyway = Flyway.configure().dataSource(dataSource).load()
        flyway.migrate()
    }

    private fun connectToDatabase(dataSource: HikariDataSource): Database =
        connect(
            datasource = dataSource,
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
                useNestedTransactions = true
                defaultIsolationLevel = TransactionSerializable
                defaultRepetitionAttempts = 2
            }
        )
}
