package com.conancleanup.repo

interface ConanRepository {
    fun readAccounts(): Collection<AccountEO>
    fun readGuilds(): Collection<GuildEO>
    fun readPlayers(): Collection<PlayerEO>
    fun readBuildingsAndPlaceables(): Collection<BuildingOrPlaceableEO>
    fun readBuildingInstances(): Collection<BuildingInstancesEO>
    fun updateOwnership(buildingIds: List<Long>, owner: Long)
    fun deletePlayer(playerId: Long, ownerId: Long, removeFromGuild: Boolean)
    fun compress()
    fun deleteGuild(guildId: Long)
    fun checkAllOwned(allOwners: Set<Long>)
}