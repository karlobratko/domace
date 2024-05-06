package hr.algebra.domace.infrastructure.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import hr.algebra.domace.domain.security.LastingFor
import hr.algebra.domace.infrastructure.persistence.exposed.ExposedRefreshTokenPersistence
import hr.algebra.domace.infrastructure.persistence.exposed.ExposedRegistrationTokenPersistence
import hr.algebra.domace.infrastructure.persistence.exposed.ExposedUserPersistence
import hr.algebra.domace.infrastructure.persistence.exposed.RegistrationConfig
import hr.algebra.domace.infrastructure.security.jwt.SecurityConfig
import hr.algebra.domace.infrastructure.serialization.Resources
import java.sql.Connection
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.koin.dsl.module

val PersistenceModule =
    module {
        single {
            val config: hr.algebra.domace.infrastructure.persistence.DatabaseConfig = Resources.hocon("db/db.dev.conf")
            val secrets: DatabaseCredentialsConfig = Resources.hocon("secrets/db.dev.conf")

            val dataSource = HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = config.jdbcUrl
                    config.dataSource.also {
                        driverClassName = it.driverClass
                    }
                    secrets.also {
                        username = it.username
                        password = it.password
                    }
                    maximumPoolSize = config.maximumPoolSize
                }
            )

            val flyway = Flyway.configure().dataSource(dataSource).load()
            flyway.migrate()

            Database.connect(
                datasource = dataSource,
                databaseConfig = DatabaseConfig {
                    sqlLogger = StdOutSqlLogger
                    useNestedTransactions = false
                    defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                    defaultRepetitionAttempts = 2
                }
            )
        }

        single(createdAtStart = true) {
            val config: SecurityConfig = Resources.hocon("security/security.conf")

            RegistrationConfig(LastingFor(config.lasting.registration))
        }

        single { ExposedRegistrationTokenPersistence(get(), get()) }

        single { ExposedUserPersistence(get(), get()) }

        single { ExposedRefreshTokenPersistence(get()) }
    }
