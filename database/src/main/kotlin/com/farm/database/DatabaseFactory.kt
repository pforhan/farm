// farm/database/src/main/kotlin/com/farm/database/DatabaseFactory.kt
package com.farm.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

  fun init() {
    // Retrieve database credentials from environment variables, with defaults
    val jdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:mysql://db:3306/farm_db"
    val user = System.getenv("MYSQL_USER") ?: "farm_user_dev"
    val password = System.getenv("MYSQL_PASSWORD") ?: "farm_password_dev"

    // For debugging, print environment variables (avoid in production for passwords)
    println("Initializing database connection:")
    println("JDBC URL: $jdbcUrl")
    println("User: $user")
    // println("Password: $password") # Keep password out of logs for security

    // Manual check for JDBC driver (useful for debugging NoClassDefFoundError)
    try {
      Class.forName("com.mysql.cj.jdbc.Driver")
      println("MySQL JDBC Driver found.")
    } catch (e: ClassNotFoundException) {
      System.err.println(
        "ERROR: MySQL JDBC Driver NOT found. Please ensure 'mysql-connector-java' is in your dependencies. Message: ${e.message}"
      )
      throw e
    }

    // Attempt to connect to the database using HikariCP
    try {
      Database.connect(hikari(jdbcUrl, user, password))
      println("Database connection established successfully.")
    } catch (e: Exception) {
      System.err.println(
        "ERROR: Failed to connect to the database. Please check connection details and ensure MySQL service is running. Message: ${e.message}"
      )
      throw e
    }

    // Create tables if they don't exist. This transaction ensures schema creation
    // happens only once on startup.
    try {
      transaction {
        SchemaUtils.create(
          Stores, Authors, Licenses, Tags, Projects, Assets, Files, AssetTags, AssetProjects
        )
        println("Database schema created/verified successfully.")
      }
    } catch (e: Exception) {
      System.err.println("ERROR: Failed to create or verify database schema. Message: ${e.message}")
      throw e
    }
  }

  private fun hikari(
    jdbcUrl: String,
    user: String,
    password: String
  ): HikariDataSource {
    val config = HikariConfig()
    config.jdbcUrl = jdbcUrl
    config.username = user
    config.password = password
    config.driverClassName = "com.mysql.cj.jdbc.Driver"
    config.maximumPoolSize = 3
    config.isAutoCommit = false
    config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    config.validate()
    return HikariDataSource(config)
  }

  // Helper function for performing database queries within a coroutine context
  suspend fun <T> dbQuery(block: Transaction.() -> T): T =
  // Dispatches the database operation to an IO-bound thread pool
    // This prevents blocking the main event loop in Ktor.
    withContext(Dispatchers.IO) {
      transaction { block() }
    }
}
