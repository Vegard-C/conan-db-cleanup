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
    @Value("\${conancleanup.holdplayerids}")
    private lateinit var holdPlayerIdsString: String
    @Value("\${conancleanup.unownedfixguildid}")
    private lateinit var unownedFixGuildIdString: String
    @Value("\${conancleanup.maxdaysoffline}")
    private lateinit var maxDaysOfflineString: String

    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        do {
            val fixDone = processDb()
        } while (fixDone)
    }

    private fun processDb(): Boolean {
        val server = service.readServer()
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

        if (unownedIds.isNotEmpty()) {
            val fixGuildId = unownedFixGuildIdString.toLong()
            val fixGuild = server.guilds().first { it.id == fixGuildId }
            println("Fixing unowned placeables. Give it to Guild ${fixGuild.name}")
            server.transferOwnership(unownedIds, server.ownership(fixGuild))
            return true
        } else {
            val holdPlayersIds = holdPlayerIdsString.split(",").map { it.trim().toLong() }.toSet()
            val maxOffline = maxDaysOfflineString.toLong()
            val firstPlayerToDelete = server.players().find { !holdPlayersIds.contains(it.id) && it.daysOffline > maxOffline }
            if (firstPlayerToDelete != null) {
                println("Player ${firstPlayerToDelete.name} is ${firstPlayerToDelete.daysOffline} offline and will be deleted")
                server.deletePlayer(firstPlayerToDelete)
                return true
            } else {
                println("Nothing more to do. Compressing DB")
                server.compress()
                return false
            }
        }
    }
}

fun interface DbConnectionBuilder {
    fun connection(): Connection
}