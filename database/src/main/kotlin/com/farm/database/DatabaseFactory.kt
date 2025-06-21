// farm/database/src/main/kotlin/com/farm/database/DatabaseFactory.kt
package com.farm.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils.create
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    fun init() {
        // Use environment variables for database connection
        val farmJdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:mysql://db:3306/farm_db"
        val dbUser = System.getenv("MYSQL_USER") ?: "farm_user"
        val dbPassword = System.getenv("MYSQL_PASSWORD") ?: "farm_password"

        val config = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl = farmJdbcUrl
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
            create(Stores)
            create(Licenses)
            create(Authors)
            create(Tags)
            create(Projects)
            create(Assets)
            create(Files)
            create(AssetTags)
            create(AssetProjects)
        }
        println("Database initialized and tables ensured.")
    }

    // A utility function to run database transactions on a separate thread pool
    // This is crucial for Ktor's non-blocking nature.
    // TODO This is deprecated and should be replaced with `suspendTransaction()` from exposed-r2dbc instead to use a suspending transaction.
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
