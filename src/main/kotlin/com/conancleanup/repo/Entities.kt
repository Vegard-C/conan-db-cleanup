package com.conancleanup.repo

data class AccountEO(
    val id: Long,
    val funcomId: String,
)
data class GuildEO(
    val id: Long,
    val name: String,
)
data class PlayerEO(
    val id: Long,
    val ownerId: Long,
    val name: String,
    val lastOnlineEpocheSeconds: Long,
    val guildId: Long? = null,
)
data class BuildingOrPlaceableEO(
    val id: Long,
    val owner: Long,
)
data class BuildingInstancesEO(
    val id: Long,
    val countInstances: Long,
)