package com.conancleanup.repo

interface ConanRepository {
    fun readAccounts(): Collection<AccountEO>
    fun readGuilds(): Collection<GuildEO>
    fun readPlayers(): Collection<PlayerEO>
}