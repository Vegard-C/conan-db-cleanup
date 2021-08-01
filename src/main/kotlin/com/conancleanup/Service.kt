package com.conancleanup

import org.springframework.stereotype.Service
import java.sql.Connection
import java.sql.ResultSet
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class Service(private val conBuilder: DbConnectionBuilder) {
    fun readServer(): Server {
        conBuilder.connection().use { c ->
            val accounts = c.readTable("select id, user from account") { Account(it.getLong(1), it.getString(2)) }
            val guilds = c.readTable("select guildId, name from guilds") { Guild(it.getLong(1), it.getString(2)) }
            val guildId2Guild = guilds.associateBy { it.id }

            val players = readPlayers(c, accounts.associateBy { it.id }, guildId2Guild)

            return Server(
                accounts = accounts.sortedBy { it.id },
                guilds = guilds.sortedBy { it.name },
                players = players.sortedBy { "${it.name}.${it.account.funcomId}" }
            )
        }
    }

    private fun readPlayers(
        c: Connection,
        accountId2Account: Map<Long, Account>,
        guildId2Guild: Map<Long, Guild>
    ) = c.readTable("select playerId, char_name, lastTimeOnline, guild from characters") { rs ->
        val id = rs.getLong(1)
        val name = rs.getString(2)
        val epocheSeconds = rs.getLong(3)
        val guildId: Any? = rs.getObject(4)
        val lastOnline = Instant.ofEpochSecond(epocheSeconds)
        Player(
            id = id,
            name = name,
            account = accountId2Account.getValue(id),
            lastOnlineTs = lastOnline,
            daysOffline = ChronoUnit.DAYS.between(lastOnline, Instant.now()),
            guild = if (guildId == null) null else guildId2Guild.getValue((guildId as Int).toLong()),
        )
    }

    private fun <T> Connection.readTable(sql: String, buildItem: (ResultSet) -> T): List<T> {
        val l = mutableListOf<T>()
        createStatement().use { s ->
            s.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    l.add(buildItem(rs))
                }
            }
        }
        return l
    }
}