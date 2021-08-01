package com.conancleanup

import java.time.Instant

data class Server(
    val accounts: List<Account>, // ordered by id
    val guilds: List<Guild>, // ordered by name
    val players: List<Player>, // ordered by name and funcomId
)
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