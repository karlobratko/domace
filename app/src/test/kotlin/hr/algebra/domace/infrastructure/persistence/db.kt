package hr.algebra.domace.infrastructure.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.StdOutSqlLogger
import java.sql.Connection

object Database {
    val test by lazy {
        val dataSource =
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = "jdbc:h2:mem:test"
                    driverClassName = "org.h2.Driver"
                    username = "username"
                    password = "password"
                    maximumPoolSize = 6
                    isReadOnly = true
                }
            )

        Database.connect(
            datasource = dataSource,
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
                useNestedTransactions = true
                defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                defaultRepetitionAttempts = 2
            }
        )
    }
}
