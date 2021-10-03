package com.conancleanup

import java.time.Instant

interface Server {
    fun accounts(): List<Account> // ordered by id
    fun guilds(): List<Guild> // ordered by name
    fun players(): List<Player> // ordered by name and funcomId
    fun playersFromGuild(guild: Guild): List<Player>
    fun ownership(player: Player): Owner
    fun ownership(guild: Guild): Owner
    fun unknownOwnership(): Owner
    fun ownedPlaceables(ownership: Owner): List<Placeable>
    fun ownedBuildings(ownership: Owner): List<Building>
    fun transferOwnership(ids: List<Long>, ownership: Owner)
    fun deletePlayer(player: Player, removeFromGuild: Boolean)
    fun compress()
    fun deleteGuild(guild: Guild)
    fun checkAllOwned(allOwners: Set<Long>)
}
data class Account(
    val id: Long,
    val funcomId: String,
)
data class Guild(
    val id: Long,
    val name: String,
)
data class Player(
    val id: Long,
    val ownerId: Long,
    val name: String,
    val account: Account,
    val lastOnlineTs: Instant,
    val daysOffline: Long,
    val guild: Guild? = null,
) {
    override fun toString(): String {
        return "Player(name='$name' guild='${guild?.name ?: "-"}' daysOffline=${daysOffline} funcomID=${account.funcomId})"
    }
}

sealed class Owner
class OwnerPlayer(val player: Player) : Owner()
class OwnerGuild(val guild: Guild) : Owner()
class OwnerUnknown : Owner()

data class Placeable(
    val id: Long,
    val owner: Owner,
)
data class Building(
    val id: Long,
    val owner: Owner,
    val countPieces: Long,
)