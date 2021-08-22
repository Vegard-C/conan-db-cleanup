package com.conancleanup.repo

interface ConanRepository {
    fun readAccounts(): Collection<AccountEO>
    fun readGuilds(): Collection<GuildEO>
    fun readPlayers(): Collection<PlayerEO>
    fun readBuildingsAndPlaceables(): Collection<BuildingOrPlaceableEO>
    fun readBuildingInstances(): Collection<BuildingInstancesEO>
}