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
        DbConnectionBuilder { DriverManager.getConnection(dbUrl) }
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
            val buildings = server.ownedBuildings(server.ownership(guild)).map { it.countPieces }.sum()
            val placeables = server.ownedPlaceables(server.ownership(guild)).size
            println("${guild.name}: $buildings buildings, $placeables placeables")
        }
        server.players().forEach { player ->
            val buildings = server.ownedBuildings(server.ownership(player)).map { it.countPieces }.sum()
            val placeables = server.ownedPlaceables(server.ownership(player)).size
            println("${player.name}: $buildings buildings, $placeables placeables")
        }
        val buildings = server.ownedBuildings(server.unknownOwnership()).map { it.countPieces }.sum()
        val placeables = server.ownedPlaceables(server.unknownOwnership()).size
        val unownedIds = server.ownedBuildings(server.unknownOwnership()).map { it.id } +
                server.ownedPlaceables(server.unknownOwnership()).map { it.id }
        println("Not owned: $buildings buildings, $placeables placeables")
        println("Not owned building IDs: $unownedIds")
    }
}

fun interface DbConnectionBuilder {
    fun connection(): Connection
}