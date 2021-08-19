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

        return ServerImpl(
            accounts = accounts.sortedBy { it.id },
            guilds = guilds.sortedBy { it.name },
            players = players.sortedBy { "${it.name}.${it.account.funcomId}" }
        )
    }

    private class ServerImpl(
        private val accounts: List<Account>,
        private val guilds: List<Guild>,
        private val players: List<Player>
    ) : Server {

        override fun accounts(): List<Account> = accounts

        override fun guilds(): List<Guild> = guilds

        override fun players(): List<Player> = players

        override fun playersFromGuild(guild: Guild): List<Player> = players.filter { it.guild == guild }

        override fun toString(): String {
            return "ServerImpl(accounts=$accounts, guilds=$guilds, players=$players)"
        }



    }
}