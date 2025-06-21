// farm/database/src/main/kotlin/com/farm/database/DatabaseFactory.kt
package com.farm.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.v1.core.Database
import org.jetbrains.exposed.sql.v1.core.SchemaUtils
import org.jetbrains.exposed.sql.v1.core.transactions.transaction
import org.jetbrains.exposed.sql.v1.core.transactions.experimental.newSuspendedTransaction
import kotlinx.coroutines.Dispatchers // Still need this for Dispatchers.IO

object DatabaseFactory {

    fun init() {
        // Use environment variables for database connection
        val jdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:mysql://db:3306/farm_db"
        val dbUser = System.getenv("MYSQL_USER") ?: "farm_user"
        val dbPassword = System.getenv("MYSQL_PASSWORD") ?: "farm_password"

        val config = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = jdbcUrl
            username = dbUser
            password = dbPassword
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate() // Validate the configuration
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        // Create tables if they don't exist
        transaction {
            SchemaUtils.create(Stores)
            SchemaUtils.create(Licenses)
            SchemaUtils.create(Authors)
            SchemaUtils.create(Tags)
            SchemaUtils.create(Projects)
            SchemaUtils.create(Assets)
            SchemaUtils.create(Files)
            SchemaUtils.create(AssetTags)
            SchemaUtils.create(AssetProjects)
        }
        println("Database initialized and tables ensured.")
    }

    // A utility function to run database transactions on a separate thread pool
    // This is crucial for Ktor's non-blocking nature.
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}