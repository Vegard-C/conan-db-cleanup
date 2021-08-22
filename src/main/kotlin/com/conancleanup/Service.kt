package com.conancleanup

import com.conancleanup.repo.ConanRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class Service(private val repo: ConanRepository) {
    fun readServer(): Server {
        val accountEOs = repo.readAccounts()
        val guildEOs = repo.readGuilds()
        val playerEOs = repo.readPlayers()

        val accounts = accountEOs.map { Account(id = it.id, funcomId = it.funcomId) }
        val guilds = guildEOs.map { Guild(id = it.id, name = it.name) }

        val guildId2Guild = guilds.associateBy { it.id }
        val accountId2Account = accounts.associateBy { it.id }

        val players = playerEOs.map {
            with(it) {
                Player(
                    id = id,
                    name = name,
                    account = accountId2Account.getValue(id),
                    lastOnlineTs = Instant.ofEpochSecond(lastOnlineEpocheSeconds),
                    daysOffline = ChronoUnit.DAYS.between(
                        Instant.ofEpochSecond(lastOnlineEpocheSeconds),
                        Instant.now()
                    ),
                    guild = if (guildId == null) null else guildId2Guild.getValue(guildId),
                )
            }
        }

        val ownerId2Owner = mutableMapOf<Long, Owner>()
        guilds.forEach { ownerId2Owner.put(it.id, OwnerGuild(it)) }
        players.forEach { ownerId2Owner.put(it.id, OwnerPlayer(it)) }
        val unknownOwner = OwnerUnknown()

        val buildingsAndPlaceablesId2OwnerId = repo.readBuildingsAndPlaceables().associate { it.id to it.owner }
        val buildingId2ItemCount = repo.readBuildingInstances().associate { it.id to it.countInstances }

        val placeables = buildingsAndPlaceablesId2OwnerId
            .filter { (id, _) -> !buildingId2ItemCount.containsKey(id) }
            .map { (id, ownerId) -> Placeable(id, ownerId2Owner.getOrDefault(ownerId, unknownOwner)) }
        val buildings = buildingsAndPlaceablesId2OwnerId
            .filter { (id, _) -> buildingId2ItemCount.containsKey(id) }
            .map { (id, ownerId) -> Building(id, ownerId2Owner.getOrDefault(ownerId, unknownOwner), buildingId2ItemCount.getValue(id)) }

        return ServerImpl(
            accounts = accounts.sortedBy { it.id },
            guilds = guilds.sortedBy { it.name },
            players = players.sortedBy { "${it.name}.${it.account.funcomId}" },
            ownerId2Owner = ownerId2Owner,
            unknownOwner = unknownOwner,
            placeables = placeables,
            buildings = buildings,
        )
    }

    private class ServerImpl(
        private val accounts: List<Account>,
        private val guilds: List<Guild>,
        private val players: List<Player>,
        private val ownerId2Owner : Map<Long, Owner>,
        private val unknownOwner: Owner,
        private val placeables: List<Placeable>,
        private val buildings: List<Building>,
    ) : Server {

        override fun accounts(): List<Account> = accounts
        override fun guilds(): List<Guild> = guilds
        override fun players(): List<Player> = players
        override fun playersFromGuild(guild: Guild): List<Player> = players.filter { it.guild == guild }

        override fun ownership(player: Player): Owner = ownerId2Owner.getValue(player.id)
        override fun ownership(guild: Guild): Owner = ownerId2Owner.getValue(guild.id)
        override fun unknownOwnership(): Owner = unknownOwner

        override fun ownedPlaceables(ownership: Owner): List<Placeable> = placeables.filter { it.owner == ownership }

        override fun ownedBuildings(ownership: Owner): List<Building> = buildings.filter { it.owner == ownership }

        override fun toString(): String {
            return "ServerImpl(accounts=$accounts, guilds=$guilds, players=$players)"
        }
    }
}