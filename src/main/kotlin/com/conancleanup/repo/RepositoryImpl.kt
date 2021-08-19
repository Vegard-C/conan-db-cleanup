package com.conancleanup.repo

import com.conancleanup.DbConnectionBuilder
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.ResultSet

@Repository
class RepositoryImpl(private val conBuilder: DbConnectionBuilder) : ConanRepository {
    override fun readAccounts(): Collection<AccountEO> {
        conBuilder.connection().use { c ->
            return c.readTable("select id, user from account") { AccountEO(it.getLong(1), it.getString(2)) }
        }
    }

    override fun readGuilds(): Collection<GuildEO> {
        conBuilder.connection().use { c ->
            return c.readTable("select guildId, name from guilds") { GuildEO(it.getLong(1), it.getString(2)) }
        }
    }

    override fun readPlayers(): Collection<PlayerEO> {
        conBuilder.connection().use { c ->
            return c.readTable("select playerId, char_name, lastTimeOnline, guild from characters") { rs ->
                val guild: Any? = rs.getObject(4)
                PlayerEO(
                    id = rs.getLong(1),
                    name = rs.getString(2),
                    lastOnlineEpocheSeconds = rs.getLong(3),
                    guildId = if (guild == null) null else (guild as Int).toLong(),
                )
            }
        }
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