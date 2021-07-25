package com.conancleanup

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.DriverManager

@Configuration
class AppConfig {
    @Bean
    fun dbConnectionBuilder(@Value("\${conancleanup.db.url}") dbUrl: String) =
        object : DbConnectionBuilder {
            override fun connection() = DriverManager.getConnection(dbUrl)
        }
}

@Component
class TestRun (private val conBuilder: DbConnectionBuilder) {

    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        conBuilder.connection().use { c ->
            c.createStatement().use { s ->
                s.executeQuery("select playerId, char_name from characters").use { rs ->
                    while (rs.next()) {
                        val playerId = rs.getLong(1)
                        val name = rs.getString(2)
                        println("Player '$name' with id $playerId")
                    }
                }
            }
        }
    }
}

interface DbConnectionBuilder {
    fun connection(): Connection
}