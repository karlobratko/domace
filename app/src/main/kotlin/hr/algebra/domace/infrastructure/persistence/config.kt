package hr.algebra.domace.infrastructure.persistence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class DatabaseConfig(
    @SerialName("jdbc-url") val jdbcUrl: String,
    @SerialName("data-source") val dataSource: DataSourceConfig,
    @SerialName("maximum-pool-size") val maximumPoolSize: Int
) {
    @Serializable data class DataSourceConfig(
        @SerialName("driver-class") val driverClass: String,
        val driver: String,
        val database: String
    )
}

@Serializable data class DatabaseCredentialsConfig(val username: String, val password: String)
