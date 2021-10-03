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
class TestRun(private val service: Service) {
    @Value("\${conancleanup.holdplayerids}")
    private lateinit var holdPlayerIdsString: String

    @Value("\${conancleanup.unownedPlaceables}")
    private lateinit var unownedPlaceablesString: String

    @Value("\${conancleanup.maxdaysoffline}")
    private lateinit var maxDaysOfflineString: String

    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        deletePlayers()
        checkAndCompress()
    }

    private fun deletePlayers() {
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
        val unownedPlaceableIds = unownedPlaceablesString.split(",").map { it.trim().toLong() }.toSet()
        if (unownedIds.filter { !unownedPlaceableIds.contains(it) }.isNotEmpty()) {
            println("Not owned: $buildings buildings, $placeables placeables")
            println("Not owned building IDs: $unownedIds")
        }
        val holdPlayersIds = holdPlayerIdsString.split(",").map { it.trim().toLong() }.toSet()
        val maxOffline = maxDaysOfflineString.toLong()

        val playersToBeDeleted = server.players().filter { !holdPlayersIds.contains(it.id) && it.daysOffline > maxOffline }
        val guildsToBeDeleted = server.guilds().filter { guild ->
            server.playersFromGuild(guild).filter { !playersToBeDeleted.contains(it) }.isEmpty()
        }

        if (playersToBeDeleted.isNotEmpty() || guildsToBeDeleted.isNotEmpty()) {
            playersToBeDeleted.forEach { player ->
                println("Removing player ${player.name}")
                val removeFromGuild =
                    player.guild != null && guildsToBeDeleted.find { it.id == player.guild.id } != null
                server.deletePlayer(player, removeFromGuild)
            }
            guildsToBeDeleted.forEach(server::deleteGuild)
            val deletedPlayers = playersToBeDeleted.map { it.name }
            val deletedGuilds = guildsToBeDeleted.map { it.name }
            if (deletedPlayers.isNotEmpty()) {
                println("Players cleaned: $deletedPlayers")
                println("You MUST delete those players in Pippi later to remove them from Pippis list")
            }
            if (deletedGuilds.isNotEmpty()) {
                println("Deleted guilds: $deletedGuilds")
            }
        }
    }

    private fun checkAndCompress() {
        val server = service.readServer()
        val allOwners = mutableSetOf<Long>()
        server.guilds().forEach {
            allOwners.add(it.id)
            server.ownedBuildings(server.ownership(it)).forEach { allOwners.add(it.id) }
            server.ownedPlaceables(server.ownership(it)).forEach { allOwners.add(it.id) }
        }
        server.players().forEach {
            allOwners.add(it.ownerId)
            server.ownedBuildings(server.ownership(it)).forEach { allOwners.add(it.id) }
            server.ownedPlaceables(server.ownership(it)).forEach { allOwners.add(it.id) }
        }
        server.checkAllOwned(allOwners)
        server.compress()
    }
}

fun interface DbConnectionBuilder {
    fun connection(): Connection
}