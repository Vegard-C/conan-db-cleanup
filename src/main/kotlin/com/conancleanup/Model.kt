package com.conancleanup

import java.time.Instant

interface Server {
    fun accounts(): List<Account> // ordered by id
    fun guilds(): List<Guild> // ordered by name
    fun players(): List<Player> // ordered by name and funcomId
    fun playersFromGuild(guild: Guild): List<Player>
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