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
class TestRun (private val service: Service) {

    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        val server = service.readServer()
        println(server)
        server.guilds().forEach { guild ->
            val players = server.playersFromGuild(guild).map { it.name }
            println("${guild.name}: $players")
        }
    }
}

interface DbConnectionBuilder {
    fun connection(): Connection
}